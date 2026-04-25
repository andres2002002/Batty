package com.habitiora.batty.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class BatteryStats(
    val avgLevel: Float,
    val minLevel: Int,
    val maxLevel: Int,
    val avgTemperature: Float,
    val minTemperature: Float,
    val maxTemperature: Float,
    val avgVoltage: Float,
    val avgCurrentMa: Float,
    val peakCurrentMa: Float,
    val avgWatts: Float,
    val peakWatts: Float,
    val screenOnSamples: Int,
    val batterySaverSamples: Int,
    val totalSamples: Int,
) {
    val screenOnRatio: Float
        get() = if (totalSamples > 0) screenOnSamples.toFloat() / totalSamples else 0f

    val batterySaverRatio: Float
        get() = if (totalSamples > 0) batterySaverSamples.toFloat() / totalSamples else 0f

    val levelRange: Int get() = maxLevel - minLevel
    val temperatureRange: Float get() = maxTemperature - minTemperature
}