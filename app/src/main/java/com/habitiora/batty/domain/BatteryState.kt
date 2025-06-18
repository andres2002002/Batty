package com.habitiora.batty.domain

data class BatteryState(
    val level: Float,
    val isCharging: Boolean,
    val temperature: Float, // en °C
    val chargingSource: String
)