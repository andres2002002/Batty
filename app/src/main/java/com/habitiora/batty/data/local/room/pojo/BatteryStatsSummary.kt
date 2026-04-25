package com.habitiora.batty.data.local.room.pojo

data class BatteryStatsSummary(
    val avgLevel: Float?,
    val minLevel: Int?,
    val maxLevel: Int?,
    val avgTemperature: Float?,
    val minTemperature: Float?,
    val maxTemperature: Float?,
    val avgVoltage: Float?,
    val avgCurrentMa: Float?,
    val peakCurrentMa: Float?,
    val avgWatts: Float?,
    val peakWatts: Float?,
    val screenOnSamples: Int,
    val batterySaverSamples: Int,
    val totalSamples: Int,
)