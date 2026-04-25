package com.habitiora.batty.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class BatteryDataPoint(
    val timestamp: Long,
    val level: Int,
    val temperature: Float,
    val currentMa: Float,   // abs — sign-agnostic
    val watts: Float,
)

typealias LevelDataPoint = BatteryDataPoint
typealias TemperatureDataPoint = BatteryDataPoint
typealias CurrentDataPoint = BatteryDataPoint