package com.habitiora.batty.utils

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryHistoryStrategy @Inject constructor() {
    companion object {
        // Intervalos dinámicos
        const val INTERVAL_CHARGING_FAST = 30_000L      // 30 segundos cargando
        const val INTERVAL_CHARGING_SLOW = 60_000L      // 1 minuto carga lenta
        const val INTERVAL_DISCHARGING_ACTIVE = 120_000L // 2 minutos descarga activa
        const val INTERVAL_DISCHARGING_IDLE = 300_000L   // 5 minutos descarga idle
        const val INTERVAL_FULL_BATTERY = 600_000L       // 10 minutos batería llena
        const val INTERVAL_CRITICAL = 30_000L           // 30 segundos nivel crítico

        // Umbrales para cambios significativos
        const val MIN_LEVEL_CHANGE = 1                   // Mínimo 1% de cambio
        const val MIN_TEMPERATURE_CHANGE = 30            // 3°C de cambio
    }

    fun shouldSaveEntry(
        currentLevel: Int,
        lastLevel: Int,
        isCharging: Boolean,
        lastStateCharging: Boolean,
        lastTimestamp: Long,
        temperature: Int,
        lastTemperature: Int,
        screenOn: Boolean
    ): Boolean {
        val timeSinceLastEntry = System.currentTimeMillis() - lastTimestamp
        val levelChange = kotlin.math.abs(currentLevel - lastLevel)
        val tempChange = kotlin.math.abs(temperature - lastTemperature)

        return when {
            // Cambio significativo en nivel
            levelChange >= MIN_LEVEL_CHANGE -> true

            // Cambio significativo en temperatura
            tempChange >= MIN_TEMPERATURE_CHANGE -> true

            // Cambio en estado de carga
            lastStateCharging != isCharging -> true

            // Intervalos basados en contexto
            isCharging && timeSinceLastEntry >= getChargingInterval() -> true
            !isCharging && timeSinceLastEntry >= getDischargingInterval(screenOn, currentLevel) -> true

            else -> false
        }
    }

    private fun getChargingInterval(): Long {
        // Lógica para determinar velocidad de carga
        return INTERVAL_CHARGING_FAST
    }

    private fun getDischargingInterval(screenOn: Boolean, level: Int): Long {
        return when {
            level <= 15 -> INTERVAL_CRITICAL
            level >= 95 -> INTERVAL_FULL_BATTERY
            screenOn -> INTERVAL_DISCHARGING_ACTIVE
            else -> INTERVAL_DISCHARGING_IDLE
        }
    }
}