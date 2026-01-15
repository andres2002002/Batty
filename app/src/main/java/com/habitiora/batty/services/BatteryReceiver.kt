package com.habitiora.batty.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.PowerManager
import com.habitiora.batty.data.manager.BatteryTimeEstimator
import com.habitiora.batty.data.manager.SessionManager
import com.habitiora.batty.domain.model.BatteryState
import com.habitiora.batty.domain.model.ChargingType
import com.habitiora.batty.domain.model.BatteryHealth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryReceiver @Inject constructor(
    private val batteryEstimator: BatteryTimeEstimator,
    private val sessionManager: SessionManager,
) {

    private val _batteryState = MutableStateFlow<BatteryState?>(null)
    val batteryState: StateFlow<BatteryState?> = _batteryState

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                val timestamp = System.currentTimeMillis()
                val batteryState = createBatteryState(context, intent, timestamp)
                _batteryState.value = batteryState
            } catch (e: Exception) {
                Timber.e(e, "Error processing battery state: ${e.message}")
            }
        }
    }

    fun getBatteryReceiver(): BroadcastReceiver = batteryReceiver

    fun createBatteryState(context: Context, intent: Intent, timestamp: Long): BatteryState {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val batteryPct = if (scale > 0) (level * 100) / scale else 0

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val chargingType = determineChargingType(intent, isCharging)
        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val health = determineBatteryHealth(intent)

        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val current = getBatteryCurrent(bm)
        val capacity = getBatteryCapacity(bm)

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val screenOn = pm.isInteractive
        val powerSave = pm.isPowerSaveMode

        // Update estimator and get calculations
        batteryEstimator.recordSample(batteryPct, timestamp)
        val (eta, chargeRate, dischargeRate) = calculateBatteryRates(isCharging)

        val sessionId = sessionManager.sessionId.value

        return BatteryState(
            timestamp = timestamp,
            batteryLevel = batteryPct,
            isCharging = isCharging,
            chargingType = chargingType,
            temperature = temp,
            voltage = voltage,
            current = current,
            capacity = capacity,
            screenOn = screenOn,
            powerSaveMode = powerSave,
            batteryHealth = health,
            chargingRate = chargeRate,
            dischargeRate = dischargeRate,
            estimatedTimeRemaining = eta,
            sessionId = sessionId
        )
    }

    private fun determineChargingType(intent: Intent, isCharging: Boolean): ChargingType {
        if (!isCharging) return ChargingType.NOT_CHARGING

        return when (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
            BatteryManager.BATTERY_PLUGGED_USB -> ChargingType.USB_CHARGER
            BatteryManager.BATTERY_PLUGGED_AC -> ChargingType.AC_CHARGER
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargingType.WIRELESS_CHARGER
            else -> ChargingType.UNKNOWN
        }
    }

    private fun determineBatteryHealth(intent: Intent): BatteryHealth {
        return when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealth.UNSPECIFIED_FAILURE
            else -> BatteryHealth.UNKNOWN
        }
    }

    private fun getBatteryCurrent(batteryManager: BatteryManager): Int? {
        return try {
            val current = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            if (current != Int.MIN_VALUE) current else null
        } catch (e: Exception) {
            Timber.w(e, "Failed to get battery current")
            null
        }
    }

    private fun getBatteryCapacity(batteryManager: BatteryManager): Long? {
        return try {
            val capacity = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            if (capacity != Long.MIN_VALUE) capacity else null
        } catch (e: Exception) {
            Timber.w(e, "Failed to get battery capacity")
            null
        }
    }

    private fun calculateBatteryRates(isCharging: Boolean): Triple<Long?, Float?, Float?> {
        val estimator = batteryEstimator

        val eta = if (isCharging) {
            estimator.estimateTimeToFullMillis()?.div(60000) // convert to minutes
        } else {
            estimator.estimateTimeToEmptyMillis()?.div(60000) // convert to minutes
        }

        val rate = estimator.estimateRatePerHour()
        val chargeRate = if (isCharging && rate != null && rate > 0) rate else null
        val dischargeRate = if (!isCharging && rate != null && rate < 0) kotlin.math.abs(rate) else null

        return Triple(eta, chargeRate, dischargeRate)
    }
}
