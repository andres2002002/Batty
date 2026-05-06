package com.habitiora.batty.domain.model

import androidx.compose.runtime.Immutable
import kotlin.math.abs

@Immutable
data class BatteryInfo(
    val id: Long = 0L,
    // ── Core ────────────────────────────────────────────────────────────
    val level: Int,
    val status: BatteryStatus,
    val health: BatteryHealth,
    val plugged: BatteryPlugged,
    val technology: String,
    val timestamp: Long = System.currentTimeMillis(),
    // ── Thermal / Electrical ────────────────────────────────────────────
    val temperature: Float,           // °C
    val voltage: Int,                 // mV
    /**
     * Corriente instantánea en mA.
     * Convención Linux: negativo = descargando, positivo = cargando.
     * Algunos OEMs invierten el signo — usar [drainRateMa] para magnitud
     * y [status] para determinar dirección real.
     */
    val currentNowMa: Float,
    val currentAvgMa: Float,          // mA promedio (ventana de ~1 min según OEM)
    /**
     * Capacidad restante leída de BATTERY_PROPERTY_CHARGE_COUNTER (µAh → mAh).
     * -1 = no disponible en este dispositivo.
     */
    val chargeCounterMah: Int,
    val watts: Float,                 // W = |V * I| / 1e6 — potencia instantánea
    // ── System State ────────────────────────────────────────────────────
    val isScreenOn: Boolean,
    val isBatterySaver: Boolean,      // PowerManager.isPowerSaveMode
    val isDozeMode: Boolean,          // PowerManager.isDeviceIdleMode
    // ── Extended (device/API dependent — -1 si no disponible) ───────────
    val cycleCount: Int,              // API 34 intent / sysfs fallback
    val chargeTimeRemainingMs: Long,  // API 28 BatteryManager.computeChargeTimeRemaining()
    val fullCapacityMah: Int,         // sysfs charge_full (capacidad actual degradada)
    val designCapacityMah: Int,       // sysfs charge_full_design (capacidad original)
) {
    val isCharging: Boolean
        get() = status == BatteryStatus.CHARGING || status == BatteryStatus.FULL

    /** Magnitud de corriente sin signo OEM-dependiente */
    val drainRateMa: Float get() = abs(currentNowMa)

    /**
     * Porcentaje de salud de batería calculado.
     * Prioridad: batteryHealthPercent (API 34) → sysfs ratio → -1
     */
    val capacityHealthPercent: Int
        get() = when {
            fullCapacityMah > 0 && designCapacityMah > 0 ->
                ((fullCapacityMah.toFloat() / designCapacityMah) * 100)
                    .toInt().coerceIn(0, 100)
            else -> -1
        }

    val healthDescription: String
        get() = when {
            capacityHealthPercent >= 80 -> "Good"
            capacityHealthPercent >= 60 -> "Fair"
            capacityHealthPercent >= 40 -> "Poor"
            capacityHealthPercent > 0   -> "Critical"
            else                        -> "Unknown"
        }

    /** mAh estimados restantes según charge counter o level × full capacity */
    val estimatedRemainingMah: Int
        get() = when {
            chargeCounterMah > 0 -> chargeCounterMah
            fullCapacityMah > 0  -> (fullCapacityMah * level / 100f).toInt()
            else -> -1
        }

    /** Tiempo estimado restante en minutos (carga o descarga) */
    val estimatedMinutesRemaining: Int
        get() {
            if (chargeTimeRemainingMs > 0) return (chargeTimeRemainingMs / 60_000L).toInt()
            val rate = drainRateMa
            return when {
                isCharging && rate > 0 && estimatedRemainingMah > 0 -> {
                    val toFull = (fullCapacityMah - estimatedRemainingMah).coerceAtLeast(0)
                    ((toFull.toFloat() / rate) * 60).toInt()
                }
                !isCharging && rate > 0 && estimatedRemainingMah > 0 ->
                    ((estimatedRemainingMah.toFloat() / rate) * 60).toInt()
                else -> -1
            }
        }

    companion object {
        fun empty() = BatteryInfo(
            level = 0, status = BatteryStatus.UNKNOWN, health = BatteryHealth.UNKNOWN,
            plugged = BatteryPlugged.NONE, technology = "Unknown",
            temperature = 0f, voltage = 0,
            currentNowMa = 0f, currentAvgMa = 0f, chargeCounterMah = -1, watts = 0f,
            isScreenOn = true, isBatterySaver = false, isDozeMode = false,
            cycleCount = -1, chargeTimeRemainingMs = -1L,
            fullCapacityMah = -1, designCapacityMah = -1
        )
    }
}