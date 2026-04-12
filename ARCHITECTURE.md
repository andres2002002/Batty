# Architecture

Batty is built on **Clean Architecture** with **MVVM** presentation pattern and strict **Unidirectional Data Flow**. This document covers the layer contracts, data flow, key design decisions, and the rationale behind discarded alternatives.

---

## Table of Contents

1. [Layered Overview](#1-layered-overview)
2. [Domain Layer](#2-domain-layer)
3. [Data Layer](#3-data-layer)
4. [Service Layer](#4-service-layer)
5. [Presentation Layer](#5-presentation-layer)
6. [Dependency Injection](#6-dependency-injection)
7. [Data Flow](#7-data-flow)
8. [Key Design Decisions](#8-key-design-decisions)
9. [Discarded Approaches](#9-discarded-approaches)

---

## 1. Layered Overview

```
┌─────────────────────────────────────────────────────────┐
│                       UI                                │
│         ViewModels · Screens · UiState · NavGraph       │
└────────────────────────┬────────────────────────────────┘
                         │ Use Cases
┌────────────────────────▼────────────────────────────────┐
│                      Domain                             │
│         Models · Repository Interfaces · Use Cases      │
│              (zero Android dependencies)                │
└────────────────────────┬────────────────────────────────┘
                         │ Implementations
┌────────────────────────▼────────────────────────────────┐
│                       Data                              │
│    Room · DataStore · BatteryInfoSource · Serializers   │
│                 ThresholdsManager                       │
└─────────────────────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                      Service                            │
│      BatteryMonitorService · NotificationHelper         │
│      BatteryServiceController · BootReceiver            │
└─────────────────────────────────────────────────────────┘
```

The **domain layer** defines the contracts. Every other layer depends inward — the domain depends on nothing.

---

## 2. Domain Layer

### Models

| Model | Purpose |
|---|---|
| `BatteryInfo` | Full battery snapshot combining all data sources |
| `BatteryDataPoint` | Unified chart point (level, temperature, current, watts, isCharging) |
| `BatteryStats` | Aggregate metrics for a time range (avg, min, max, ratios) |
| `MonitorSettings` | User preferences persisted in DataStore |
| `ThresholdsConfig` | Configurable low/high thresholds, serialized as JSON |
| `ThresholdEvent` | `LowBattery(level, isCritical, isVeryLow)` or `HighBattery(level, isFullyCharged)` |
| `ServiceState` | Runtime state of `BatteryMonitorService`: `Loading`, `Active`, `Inactive`, `Error` |
| `DndBypassState` | `@JvmInline value class` — single Boolean from `channel.canBypassDnd()` |

### BatteryInfo — computed properties

`BatteryInfo` combines multiple hardware sources and exposes computed properties to keep derivation logic out of the UI:

```kotlin
val isCharging: Boolean         // status == CHARGING || FULL
val drainRateMa: Float          // abs(currentNowMa) — sign-agnostic
val capacityHealthPercent: Int  // API 34 > sysfs ratio > -1
val estimatedRemainingMah: Int  // chargeCounter > level × fullCapacity > -1
val estimatedMinutesRemaining: Int  // chargeTimeRemainingMs > mAh/rate > -1
val watts: Float                // |V[V] × I[A]| — never negative
```

**OEM sign convention:** The Linux kernel defines negative current = discharging, positive = charging. Qualcomm, Samsung, and MediaTek invert this in their drivers. `drainRateMa` uses `abs()` unconditionally; direction is always inferred from `status`, never from the sign of the current.

**Unavailable fields convention:** Primitive fields that may not be available on all hardware use `-1` / `-1L` as the sentinel value. The UI guards every display with `> 0`. Null is never used for primitives.

### Repository interfaces

```kotlin
interface BatteryRepository {
    fun observeLiveBattery(): Flow<BatteryInfo>
    fun observeLatestSnapshot(): Flow<BatteryInfo?>
    fun observeChartData(timeRange: TimeRange): Flow<List<BatteryDataPoint>>
    suspend fun saveSnapshot(batteryInfo: BatteryInfo)
    suspend fun getStats(timeRange: TimeRange): BatteryStats
    suspend fun cleanOldData(keepDays: Int = 30)
}

interface SettingsRepository {
    fun observe(): Flow<MonitorSettings>
    suspend fun get(): MonitorSettings
    suspend fun update(settings: MonitorSettings)
    suspend fun setMonitorBattery(enabled: Boolean)
    suspend fun setAlertPolicy(alertPolicy: AlertPolicy)
    suspend fun setStartOnBoot(enabled: Boolean)
}

interface ThresholdsRepository {
    fun observeConfig(): Flow<ThresholdsConfig>
    suspend fun getConfig(): ThresholdsConfig
    suspend fun updateConfig(config: ThresholdsConfig)
}
```

### Use Cases

Each use case is a single-responsibility class with one public entry point (`operator fun invoke` or named methods). They act as the boundary between the presentation and data layers.

```
ObserveLiveBatteryUseCase       → Flow<BatteryInfo>
SaveBatterySnapshotUseCase      → suspend (BatteryInfo)
GetBatteryStatsUseCase          → suspend (TimeRange): BatteryStats
ObserveChartDataUseCase         → Flow<List<BatteryDataPoint>>
ObserveMonitorSettingsUseCase   → Flow<MonitorSettings>
UpdateMonitorSettingsUseCase    → granular suspend setters + full update
```

---

## 3. Data Layer

### Room — `battery_snapshots`

Schema version: **2**. Migration from v1 to v2 is explicit (`MIGRATION_1_2`) — `fallbackToDestructiveMigration()` is not used.

| Column | SQLite Type | Notes |
|---|---|---|
| `id` | INTEGER PK | autoincrement |
| `level` | INTEGER | 0–100 |
| `status` / `health` / `plugged` | TEXT | `Enum.name` |
| `temperature` | REAL | °C |
| `voltage` | INTEGER | mV |
| `technology` | TEXT | |
| `timestamp` | INTEGER | millis, indexed |
| `current_now_ma` | REAL | DEFAULT 0.0 (v2) |
| `current_avg_ma` | REAL | DEFAULT 0.0 (v2) |
| `charge_counter_mah` | INTEGER | DEFAULT -1 (v2) |
| `watts` | REAL | DEFAULT 0.0 (v2) |
| `is_screen_on` | INTEGER | 0/1 (v2) |
| `is_battery_saver` | INTEGER | 0/1 (v2) |
| `is_doze_mode` | INTEGER | 0/1 (v2) |
| `cycle_count` | INTEGER | DEFAULT -1 (v2) |
| `charge_time_remaining_ms` | INTEGER | DEFAULT -1 (v2) |
| `full_capacity_mah` | INTEGER | DEFAULT -1 (v2) |
| `design_capacity_mah` | INTEGER | DEFAULT -1 (v2) |
| `battery_health_percent` | INTEGER | DEFAULT -1 (v2) |

Booleans are stored as INTEGER (0/1). Mappers perform the explicit conversion — Room's built-in Boolean mapping is not relied upon for clarity.

### DataStore

Typed DataStore is used for persistence. Each repository has its own `DataStore<T>` instance, using `kotlinx-serialization` for efficient and type-safe storage.

| Repository | Model | Serializer |
|---|---|---|
| `SettingsRepository` | `MonitorSettings` | `MonitorSettingsSerializer` |
| `ThresholdsRepository` | `ThresholdsConfig` | `ThresholdsSerializer` |

The serializers handle JSON decoding/encoding via `kotlinx.serialization.json.Json`.

### BatteryInfoSource

Wraps `ACTION_BATTERY_CHANGED` (sticky broadcast) in a `callbackFlow`. Two entry points:

- `observeLive()` — `Flow<BatteryInfo>` for the dashboard ViewModel.
- `getCurrent()` — synchronous read via `registerReceiver(null, ...)` for the initial service state.
- `fromIntent(intent)` — used by the service's own `BroadcastReceiver` to avoid re-registering.

Data enrichment pipeline per broadcast:

```
Intent (ACTION_BATTERY_CHANGED)
  → level, status, health, plugged, temperature, voltage, technology
  → BatteryManager.getIntProperty() → currentNowMa, currentAvgMa, chargeCounterMah
  → abs(V × I) → watts
  → PowerManager → isScreenOn, isBatterySaver, isDozeMode
  → BatteryManager.computeChargeTimeRemaining() [API 28+]
  → Intent.EXTRA_CYCLE_COUNT [API 34+]
```

### ThresholdsManager

In-memory singleton. Maintains two sets: `triggeredLowLevels` and `triggeredHighLevels`.

**Core rule — `addAll()` in a single operation:**

```kotlin
val newlyCrossed = thresholds.filter { level <= it && it !in triggeredLowLevels }
triggeredLowLevels.addAll(newlyCrossed)   // ALL at once — prevents re-evaluation on next broadcast
return ThresholdEvent.LowBattery(...)      // ONE event regardless of how many were crossed
```

This guarantees a single notification even when the battery drops from 25% to 7% in one step, crossing thresholds 20, 15, and 10 simultaneously.

**Reset on charging-state transition:**

- `discharging → charging`: clear `triggeredLowLevels`
- `charging → discharging`: clear `triggeredHighLevels`

Thresholds therefore re-arm themselves automatically for the next cycle.

---

## 4. Service Layer

### BatteryMonitorService

A foreground service with `FOREGROUND_SERVICE_TYPE_SPECIAL_USE` (API 34+) / `0` (older).

**Lifecycle:**

```
onCreate()
  ├── batteryInfoSource.getCurrent()     // initial state without waiting for broadcast
  ├── registerBatteryReceiver()          // ACTION_BATTERY_CHANGED
  └── observeMonitorSetting()            // reactive — stops if monitorBattery → false

onStartCommand(ACTION_START)
  └── monitorSettingsRepository.get()
      ├── monitorBattery = true  → startForeground() + startPeriodicSave()
      └── monitorBattery = false → stopSelf()

onBatteryInfoChanged(info)              // called on every ACTION_BATTERY_CHANGED
  ├── thresholdsManager.evaluate()      // always — no throttle
  │   └── event? → notificationHelper.showThresholdAlert()
  └── shouldUpdateForeground()          // throttle: ±1% or isCharging change
      └── true → updateForegroundNotification()

onTaskRemoved()
  └── monitorBattery = true → startForegroundService() restart

onDestroy()
  ├── unregisterReceiver()
  └── serviceScope.cancel()
```

**Coroutine scope:** `CoroutineScope(SupervisorJob() + Dispatchers.IO)`. Cancelled in `onDestroy()`. `SupervisorJob` prevents one failing child from cancelling the scope.

**Throttle policy:** Threshold evaluation runs on every broadcast — a critical event must never be missed. The foreground notification update is throttled to changes of ≥ 1% or a change in `isCharging`, preventing unnecessary `NotificationManager.notify()` calls.

**`observeMonitorSetting` uses `drop(1)`** to skip the initial emission already handled in `onStartCommand`. Only subsequent changes trigger reactive stops.

### BatteryServiceController

Provides an interface to manage the `BatteryMonitorService` lifecycle from the UI layer.

```kotlin
interface BatteryServiceController {
    val connectionState: StateFlow<ConnectionState>
    fun startServiceAndBind()
    fun stopServiceAndUnbind()
    fun unbindOnly()
}
```

The implementation `BatteryServiceControllerImpl` handles `ServiceConnection` and ensures the service is properly started and bound based on user settings.

### NotificationHelper

Manages three notification channels:

| Channel ID | Importance | Vibration | DND Bypass |
|---|---|---|---|
| `battery_monitor_v2` | LOW | None | No |
| `battery_alerts_v2` | DEFAULT | `[0, 200]` | No |
| `battery_critical_v2` | HIGH | `[0, 300, 150, 300]` | User-controlled |

**DND strategy:**

`setBypassDnd(true)` in the channel definition is silently ignored by Android for third-party apps. The approach used is:

1. `CATEGORY_ALARM + PRIORITY_MAX` on critical alerts → implicit DND bypass on ~95% of configurations.
2. `ACTION_CHANNEL_NOTIFICATION_SETTINGS` → directs the user to the specific channel settings where they can enable "Override Do Not Disturb" manually.
3. `channel.canBypassDnd()` → the only reliable source of truth for bypass state, exposed as `DndBypassState`.

`ACCESS_NOTIFICATION_POLICY` is intentionally absent from the manifest — it is not needed for per-alert DND and risks Play Store policy rejection.

**Alert content hierarchy:**

```
ThresholdEvent.LowBattery
  isCritical  → CRITICAL channel · PRIORITY_MAX · CATEGORY_ALARM
  isVeryLow   → CRITICAL channel · PRIORITY_HIGH · CATEGORY_ALARM
  else        → ALERTS channel  · PRIORITY_DEFAULT · CATEGORY_REMINDER

ThresholdEvent.HighBattery
  isFullyCharged → ALERTS channel · PRIORITY_DEFAULT · CATEGORY_REMINDER
  else           → ALERTS channel · PRIORITY_DEFAULT · CATEGORY_REMINDER
```

No `setColor()`, no `setColorized()` on alerts. System-style aesthetic: text is functional and direct, indistinguishable from Android system notifications.

### BootReceiver

Uses `goAsync()` to extend the broadcast receiver timeout from 5s to ~30s, allowing DataStore reads on `Dispatchers.IO` without blocking the main thread. `pendingResult.finish()` is called in `finally` to avoid wake lock leaks.

Boot logic:

```
startOnBoot = false           → skip
startOnBoot = true
  monitorBattery = false      → skip (no point starting without monitoring)
  monitorBattery = true       → startForegroundService()
```

Listens for: `BOOT_COMPLETED`, `MY_PACKAGE_REPLACED` (covers app updates), `QUICKBOOT_POWERON` (HTC / some MediaTek).

---

## 5. Presentation Layer

### UiState contracts

All UiState types use `@Stable` on sealed interfaces and `@Immutable` on data class variants.

```kotlin
// Dashboard
sealed interface BatteryUiState {
    data object Loading : BatteryUiState
    data class Success(val liveInfo: BatteryInfo, val serviceState: ServiceState) : BatteryUiState
    data class Error(val message: String) : BatteryUiState
}

// Stats
sealed interface StatsUiState {
    data object Loading : StatsUiState
    data class Success(
        val stats: BatteryStats,
        val chartData: List<BatteryDataPoint>,
        val selectedRange: TimeRange,
        val selectedChart: ChartType,
    ) : StatsUiState
    data class Error(val message: String) : StatsUiState
}

// Settings
sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(
        val settings: MonitorSettings,
        val thresholds: ThresholdsConfig,
        val dndBypass: DndBypassState
    ) : SettingsUiState
}
```

### ViewModels

ViewModels expose a single `StateFlow<UiState>` built from `combine()` + `flatMapLatest()` + `stateIn()` with `SharingStarted.WhileSubscribed(5_000)`. They never hold Android context references except when injected via `@ApplicationContext` for service intents.

**`DashboardViewModel`** combines three flows and handles initial service synchronization:

```kotlin
init {
    syncInitialServiceState()
}

val uiState: StateFlow<BatteryUiState> = combine(
    observeLiveBatteryUseCase(),
    observeMonitorSettingsUseCase().map { it.monitorBattery }.distinctUntilChanged(),
    batteryServiceController.connectionState
) { info, monitorBattery, connection ->
    BatteryUiState.Success(
        liveInfo = info,
        serviceState = resolveServiceState(monitorBattery, connection)
    )
}
```

`syncInitialServiceState()` ensures the service is started and bound if monitoring is enabled when the app opens. `resolveServiceState()` is the single location where `monitorBattery` (DataStore intent) and `ConnectionState` (runtime truth) are reconciled into a `ServiceState`.

**`SettingsViewModel`** combines `MonitorSettings` and `ThresholdsConfig`, and includes the current `DndBypassState` in its success state.

```kotlin
val uiState: StateFlow<SettingsUiState> = combine(
    observeMonitorSettingsUseCase(),
    observeThresholdsConfigUseCase()
) { settings, thresholds ->
    SettingsUiState.Success(
        settings = settings,
        thresholds = thresholds,
        dndBypass = notificationHelper.getDndBypassState()
    )
}
```

### Chart system

`TimeSeriesChart` is a pure Canvas composable. It receives `List<TimeSeriesPoint>` and a `YAxisConfig` and handles all rendering internally. Tap and drag gestures are handled via two separate `pointerInput` modifiers — one for `detectTapGestures`, one for `detectHorizontalDragGestures` — to prevent gesture competition.

Selected point state is lifted out of the canvas via `onPointSelected: ((SelectedPointInfo?) -> Unit)?` callback. The tooltip (`PointTooltip`) is a regular Composable rendered outside the canvas, enabling `AnimatedVisibility` and full Material 3 theming.

`BatteryChartByType` wraps `TimeSeriesChart` with three typed configurations (Level, Temperature, Current) that define their own `YAxisConfig`, colors, and data projection from `BatteryDataPoint`.

### Navigation

```
BatteryRoute.Dashboard  →  "battery/dashboard"  (start destination)
BatteryRoute.Stats      →  "battery/stats"
BatteryRoute.Settings   →  "battery/settings"
```

The `monitorBattery` toggle lives on the Dashboard. All other settings live on the Settings screen. There is no deep linking or argument passing — each screen is self-contained with its own injected ViewModel.

---

## 6. Dependency Injection

| Module | Type | Provides |
|---|---|---|
| `DatabaseModule` | `@object @Provides` | `BatteryDatabase`, `BatteryDao` |
| `AppModule` | `@object @Provides` | `DataStore<MonitorSettings>`, `DataStore<ThresholdsConfig>`, `NotificationManager` |
| `RepositoryModule` | `abstract @Binds` | `BatteryRepository`, `SettingsRepository`, `ThresholdsRepository` |

`@Binds` is used for interface → implementation bindings (zero-overhead at runtime). `@Provides` is used for types that require construction logic (Room builder, DataStore delegate).

All modules are installed in `SingletonComponent`. There are no feature-scoped components.

---

## 7. Data Flow

### Live battery update (happy path)

```
Android OS
  → broadcasts ACTION_BATTERY_CHANGED
  → BatteryMonitorService.BroadcastReceiver.onReceive()
      → BatteryInfoSource.fromIntent()
          → BatteryManager.getIntProperty() (current, chargeCounter)
          → PowerManager (screen, batterySaver, doze)
          → buildBatteryInfo() → BatteryInfo
      → currentInfo = info
      → serviceScope.launch {
            thresholdsManager.evaluate(level, isCharging)
              → ThresholdEvent? → notificationHelper.showThresholdAlert()
            if shouldUpdateForeground → notificationHelper.notify()
        }

BatteryInfoSource.observeLive() (callbackFlow)
  → emits BatteryInfo to BatteryViewModel
  → combine() with monitorBattery + connectionState
  → BatteryUiState.Success
  → BatteryDashboardScreen recomposes
```

### Periodic snapshot save

```
BatteryMonitorService.startPeriodicSave()
  → every 5 minutes:
      saveBatterySnapshotUseCase(currentInfo)
        → BatteryRepositoryImpl.saveSnapshot()
          → BatteryMapper.toEntity()
          → BatteryDao.insert()
          → Room → SQLite
```

### Stats screen chart load

```
BatteryStatsViewModel
  → _selectedRange changes
  → flatMapLatest { observeChartDataUseCase(range) }
      → BatteryRepositoryImpl.observeChartData()
          → BatteryDao.observeChartDataSince(since)
          → Flow<List<BatteryDataPointEntity>>
          → map { it.map { entity -> entity.toDomain() } }
  → map { data ->
        val stats = getStatsUseCase(range)   // single aggregate query
        StatsUiState.Success(stats, data, range, chart)
    }
  → BatteryStatsScreen recomposes
```

---

## 8. Key Design Decisions

### Typed DataStore isolation

Each repository uses its own `DataStore<T>` instance with a specific serializer. This provides strong typing and isolation between settings and threshold configurations.

| Repository | DataStore File |
|---|---|
| `SettingsRepository` | `monitor_settings.json` |
| `ThresholdsRepository` | `thresholds_config.json` |

The `AppModule` ensures these are provided as `@Singleton` to prevent multiple instances from accessing the same file.

### `monitorBattery` in DataStore + ServiceConnection

These are not redundant — they serve different purposes:

| | DataStore `monitorBattery` | `BatteryServiceController` |
|---|---|---|
| Represents | User intent | Runtime process state |
| Survives reboot | Yes | No |
| Used by | `BootReceiver`, `observeMonitorSetting()` | `BatteryViewModel` for UI |
| Detects crashes | No | Yes (`onServiceDisconnected`) |

The ViewModel combines both to produce a `ServiceState` that is both accurate and persistent.

### Reactive service stop

`BatteryMonitorService.observeMonitorSetting()` collects `MonitorSettings.monitorBattery` with `drop(1)` and `distinctUntilChanged()`. When the user toggles monitoring off from the UI, the DataStore update propagates into the service's own observer, which calls `stopForeground() + stopSelf()` — no explicit stop `Intent` needed from the ViewModel. The ViewModel still sends `ACTION_STOP` as a belt-and-suspenders measure; the service handles both paths cleanly.

### `BatteryDataPoint` as a unified chart model

Rather than three separate queries for level, temperature, and current charts, a single `observeChartDataSince` query fetches all columns needed for any series. Chart type selection happens in the ViewModel by projecting the appropriate field — no additional Room IO per series switch.

### `@JvmInline value class DndBypassState`

A single-Boolean wrapper with a semantic name. Prevents passing raw `Boolean` where the meaning is non-obvious. Zero runtime overhead — erased to `Boolean` by the compiler.

---

## 9. Discarded Approaches

| Approach | Reason discarded |
|---|---|
| `setBypassDnd(true)` on the notification channel | Silently ignored by Android for third-party apps. No error, no effect. |
| `ACCESS_NOTIFICATION_POLICY` permission | Grants access to global DND rules — far broader than needed. Flagged risk for Play Store policy rejection. |
| `ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS` | Opens global DND settings, not the specific channel. Replaced by `ACTION_CHANNEL_NOTIFICATION_SETTINGS`. |
| Toggle `notifyInDoNotDisturb` in DataStore | Creates a false sense of control. The system is the only source of truth for `canBypassDnd()`. The setting was eliminated — `DndBypassState` is now a pure read of the channel state. |
| `fallbackToDestructiveMigration()` | Replaced by `MIGRATION_1_2` with 12 explicit `ALTER TABLE ADD COLUMN` statements. User data is preserved across schema versions. |
| Multiple `DataStore` instances pointing to the same file | Would cause file corruption. Replaced by typed DataStore instances with distinct files. |
| Per-threshold `addAll()` calls | Caused duplicate notifications when multiple thresholds were crossed simultaneously. Fixed by collecting all newly-crossed thresholds first, then calling `addAll()` once. |
| Separate Room queries per chart series | Replaced by a single unified `BatteryDataPointEntity` query covering all series. Chart type selection is a pure in-memory projection. |
| Emojis / `setColor` / `setColorized` in alerts | Dropped for system-style aesthetic. Alerts are visually indistinguishable from Android system notifications. |
| `BIND_AUTO_CREATE` on initial bind check | Would create the service if not running, defeating the purpose of the initial state detection. Uses flag `0` instead — bind fails fast if the service is not alive. |
