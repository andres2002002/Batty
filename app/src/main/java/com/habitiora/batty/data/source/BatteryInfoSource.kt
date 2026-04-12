package com.habitiora.batty.data.source

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.habitiora.batty.domain.model.BatteryHealth
import com.habitiora.batty.domain.model.BatteryInfo
import com.habitiora.batty.domain.model.BatteryPlugged
import com.habitiora.batty.domain.model.BatteryStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class BatteryInfoSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val batteryManager =
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    private val powerManager =
        context.getSystemService(Context.POWER_SERVICE) as PowerManager

    fun observeLive(): Flow<BatteryInfo> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                trySend(buildBatteryInfo(intent))
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        awaitClose {
            runCatching { context.unregisterReceiver(receiver) }
                .onFailure { Timber.w(it, "Failed to unregister battery receiver") }
        }
    }

    fun getCurrent(): BatteryInfo =
        context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?.let { buildBatteryInfo(it) } ?: BatteryInfo.empty()

    fun fromIntent(intent: Intent): BatteryInfo = buildBatteryInfo(intent)

    // ─────────────────────────────────────────────────────────────────────────
    // Core builder
    // ─────────────────────────────────────────────────────────────────────────

    private fun buildBatteryInfo(intent: Intent): BatteryInfo {
        // ── Level ──────────────────────────────────────────────────────────────
        val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val level = if (scale > 0) (rawLevel * 100 / scale) else rawLevel

        // ── Electrical — BatteryManager properties ────────────────────────────
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

        val currentNowUa =
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentAvgUa =
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
        val chargeCounterUah =
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        val designCapacityMah =
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER) / 1000000f

        // Int.MIN_VALUE = propiedad no soportada por el hardware/driver
        val currentNowMa = if (currentNowUa != Int.MIN_VALUE) currentNowUa / 1000f else 0f
        val currentAvgMa = if (currentAvgUa != Int.MIN_VALUE) currentAvgUa / 1000f else 0f
        val chargeCounterMah = when {
            chargeCounterUah == Int.MIN_VALUE || chargeCounterUah <= 0 -> -1
            chargeCounterUah > 100_000 -> chargeCounterUah / 1000   // µAh → mAh
            else -> chargeCounterUah
        }

        // ── Watts ──────────────────────────────────────────────────────────────
        // P[W] = V[V] × I[A] = (voltage_mV / 1000) × (|currentNow_µA| / 1_000_000)
        val watts = if (voltage > 0 && currentNowUa != Int.MIN_VALUE && currentNowUa != 0) {
            abs((voltage / 1000f) * (currentNowUa / 1_000_000f))
        } else if (voltage > 0 && currentNowMa != 0f) {
            abs((voltage / 1000f) * (currentNowMa / 1000f))
        } else 0f

        // ── System state ───────────────────────────────────────────────────────
        val isScreenOn = powerManager.isInteractive
        val isBatterySaver = powerManager.isPowerSaveMode
        val isDozeMode =
            powerManager.isDeviceIdleMode

        // ── Charge time remaining ──────────────────────────────────────────────
        val chargeTimeRemainingMs =
            batteryManager.computeChargeTimeRemaining()

        // ── API 34 extras ──────────────────────────────────────────────────────
        val cycleCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            intent.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)
        } else -1

        return BatteryInfo(
            level = level,
            status = intent.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN
            ).toStatus(),
            health = intent.getIntExtra(
                BatteryManager.EXTRA_HEALTH,
                BatteryManager.BATTERY_HEALTH_UNKNOWN
            ).toHealth(),
            plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0).toPlugged(),
            temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f,
            voltage = voltage,
            technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown",
            currentNowMa = currentNowMa,
            currentAvgMa = currentAvgMa,
            chargeCounterMah = chargeCounterMah,
            watts = watts,
            isScreenOn = isScreenOn,
            isBatterySaver = isBatterySaver,
            isDozeMode = isDozeMode,
            cycleCount = cycleCount,
            chargeTimeRemainingMs = chargeTimeRemainingMs,
            fullCapacityMah = designCapacityMah.toInt(),
            designCapacityMah = designCapacityMah.toInt()
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mappers
    // ─────────────────────────────────────────────────────────────────────────

    private fun Int.toStatus(): BatteryStatus = when (this) {
        BatteryManager.BATTERY_STATUS_CHARGING -> BatteryStatus.CHARGING
        BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryStatus.DISCHARGING
        BatteryManager.BATTERY_STATUS_FULL -> BatteryStatus.FULL
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryStatus.NOT_CHARGING
        else -> BatteryStatus.UNKNOWN
    }

    private fun Int.toHealth(): BatteryHealth = when (this) {
        BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
        BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
        BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealth.UNSPECIFIED_FAILURE
        else -> BatteryHealth.UNKNOWN
    }

    private fun Int.toPlugged(): BatteryPlugged = when (this) {
        BatteryManager.BATTERY_PLUGGED_AC -> BatteryPlugged.AC
        BatteryManager.BATTERY_PLUGGED_USB -> BatteryPlugged.USB
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> BatteryPlugged.WIRELESS
        else -> BatteryPlugged.NONE
    }
}