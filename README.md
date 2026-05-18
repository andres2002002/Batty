<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="192" alt="Batty Icon">

# Batty — Battery Monitor

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg?style=flat)](https://developer.android.com/reference/android/os/Build.VERSION_CODES#O)
[![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

**Batty** is an Android battery monitoring tool designed to help you track your device's health, electrical metrics, and usage patterns in real-time. Built with a focus on clean code and a system-integrated UI, it runs in the background to monitor data and alert you based on your defined thresholds.
---

## Features

- **Real-time electrical metrics:** Live level, temperature, voltage, current (instantaneous & average), power in watts, and charge counter — sourced from `BatteryManager`, `PowerManager`, and sysfs fallback via `/sys/class/power_supply/`.
- **Battery health tracking:** Capacity degradation (current vs. design capacity in mAh), cycle count, and health percentage — with API 34 native support and sysfs fallback for older devices.
- **Configurable threshold alerts:** Set custom low and high battery thresholds. All crossed thresholds are evaluated in a single pass — one notification per event, no duplicates.
- **System-style notifications:** Persistent foreground notification with live level progress bar. Critical alerts use `CATEGORY_ALARM + PRIORITY_MAX` for implicit DND bypass on ~95% of device configurations — no intrusive system permissions required.
- **Interactive historical charts:** Room-persisted snapshots every 5 minutes. Tap or drag to scrub any point on level, temperature, or current charts across 1h / 6h / 24h / 7d / 30d ranges.
- **Charge cycle analysis:** Automatic detection of charge and discharge cycles — duration, rate (%/hr), avg power, peak temperature, and deepest discharge, built from historical data in memory.
- **System state tracking:** Screen-on ratio, Battery Saver mode, and Doze mode persisted across every snapshot.
- **Boot persistence:** Optional auto-start after device reboot, respecting the current monitoring intent stored in DataStore.

---

## Screenshots

| Dashboard | Statistics | Cycle Analysis | Settings |
|:---:|:---:|:---:|:---:|
| ![Dashboard](docs/images/screenshot_dashboard.png) | ![Statistics](docs/images/screenshot_stats.png) | ![Cycles](docs/images/screenshot_cycles.png) | ![Settings](docs/images/screenshot_settings.png) |

---

## Tech Stack & Architecture

Strict **Clean Architecture** + **MVVM** with unidirectional data flow. The domain layer has zero Android dependencies. Every component is designed for testability and long-term maintainability.

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material 3 (`@Immutable` / `@Stable` states) |
| Architecture | MVVM + Clean Architecture + UDF |
| Concurrency | Coroutines + `StateFlow` + `callbackFlow` |
| Dependency Injection | Dagger Hilt |
| Local Storage | Room v2 (explicit migrations) + DataStore Preferences |
| Serialization | `kotlinx.serialization` |
| System APIs | `BatteryManager`, `PowerManager`, `ServiceConnection`, sysfs |
| Build System | Gradle Kotlin DSL + Version Catalogs (`libs.versions.toml`) |
| Logging | Timber |

---

## Project Structure

```
com.habitiora.batty/
├── core/               # Shared logic: BatteryMapper (Entity ↔ Domain mapping)
├── data/               # Data Layer: Implementation of repositories and data sources
│   ├── datastore/      # Preferences and Proto DataStore
│   ├── local/room/     # Database: entities, DAOs, migrations, and contracts
│   ├── manager/        # ThresholdsManager and ServiceController implementation
│   ├── repository/     # Repository implementations (Battery, Settings, Thresholds)
│   └── source/         # Data sources: BatteryInfoSource (BatteryManager & Broadcasts)
├── di/                 # Hilt modules: Database, Repository, and Service modules
├── domain/             # Domain Layer: Pure business logic (POJOs & UseCases)
│   ├── model/          # Domain models (BatteryInfo, ThresholdsConfig, etc.)
│   ├── repository/     # Repository interfaces
│   └── useCase/        # Business logic: ObserveLiveBattery, GetBatteryStats, etc.
├── navigation/         # App navigation graph and route definitions
├── services/           # Android Services & System Components
│   ├── BatteryMonitorService.kt # Foreground service for background data collection
│   ├── NotificationHelper.kt    # Notification channel and alert management
│   ├── BootReceiver.kt          # Auto-start on boot logic
│   └── PermissionsHelper.kt     # Permission handling utilities
├── ui/                 # Presentation Layer: Jetpack Compose UI
│   ├── components/     # Reusable UI components (Cards, Charts, Gauges)
│   ├── screens/        # Feature screens (Dashboard, History, Settings, Info)
│   ├── theme/          # Material 3 theme and color definitions
│   └── utils/          # UI state holders and view utilities
├── utils/              # Project-wide utilities
├── BattyApp.kt         # Application class
└── MainActivity.kt     # Single Activity entry point
```

---

## How It Works

**Data collection** — `BatteryMonitorService` registers a `BroadcastReceiver` for `ACTION_BATTERY_CHANGED` and reads supplementary data from `BatteryManager` and `/sys/class/power_supply/` on every update. Snapshots are persisted to Room every 5 minutes.

**Threshold evaluation** — `ThresholdsManager` marks all crossed thresholds in a single `addAll()` call per broadcast, preventing duplicates when the battery drops multiple levels at once. Triggered levels reset on charging-state transitions so the same thresholds fire again in subsequent cycles.

**Service lifecycle** — `ServiceConnectionManager` wraps `ServiceConnection` to expose four runtime states: `Loading`, `Active`, `Inactive`, and `Error(ServiceErrorCause)`. `onServiceDisconnected` fires exclusively on process crashes, not on explicit `unbind()`, enabling clean state discrimination without DataStore polling.

**Notifications** — Three channels with escalating severity. Critical alerts use `CATEGORY_ALARM + PRIORITY_MAX` for implicit DND coverage without `ACCESS_NOTIFICATION_POLICY`. The DND setting exposes `channel.canBypassDnd()` as a read-only badge and links to `ACTION_CHANNEL_NOTIFICATION_SETTINGS` — the only way to toggle it honestly.

**Cycle analysis** — `CycleAnalyzer` detects charging/discharging transitions in the sorted dataset and builds `ChargeCycle` objects in memory. Segments with fewer than 3 points are discarded as noise. Avoids complex SQL `LAG`/`LEAD` queries that SQLite does not support natively in Room.

---

## Permissions

| Permission | Purpose |
|---|---|
| `FOREGROUND_SERVICE` | Run the battery monitor as a foreground service |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Required on API 34+ for the declared foreground service type |
| `POST_NOTIFICATIONS` | Show persistent monitor notification and threshold alerts |
| `RECEIVE_BOOT_COMPLETED` | Auto-start monitoring after device reboot (opt-in) |

`ACCESS_NOTIFICATION_POLICY` is intentionally absent — DND bypass is handled via `CATEGORY_ALARM` and user-controlled channel settings, avoiding Play Store policy risk.

---

## Build Instructions

Requires Android Studio Hedgehog or later and JDK 17.

```bash
# 1. Clone
git clone https://github.com/habitiora/batty.git

# 2. Build debug APK
./gradlew assembleDebug

# 3. Install on connected device
./gradlew installDebug
```

Open in Android Studio and sync Gradle — all dependency versions are managed via `gradle/libs.versions.toml`.

> **Note:** `SysfsBatteryReader` accesses `/sys/class/power_supply/` without special permissions on most devices. If your OEM restricts it, fields sourced from sysfs silently return `-1` and are hidden in the UI.

---

## Contributing

1. Fork the project.
2. Create your feature branch: `git checkout -b feature/AmazingFeature`
3. Follow the existing conventions: Clean Architecture layering, `@Immutable`/`@Stable` annotations, `-1` for unavailable hardware fields, `runCatching { }.onFailure { Timber.e() }` for non-critical operations.
4. Push: `git push origin feature/AmazingFeature`
5. Open a Pull Request with a clear description of the change and its rationale.

> Please open an issue first to discuss any significant change, especially anything touching the service lifecycle, notification channels, or Room schema.

---
## License

This project is licensed under the **GNU General Public License v3.0 (GPLv3)**. See the [LICENSE](LICENSE) file for the full text.

> **Note on Relicensing:** Prior to April 2026, **Batty** was licensed under the MIT License. Versions published before this date remain available under MIT terms. All subsequent versions and contributions from April 2026 onwards are strictly governed by the GPLv3 to ensure the software remains free and open for all users.