package com.habitiora.batty.data.local.room.pojo

data class BatteryDataPointEntity(
    val timestamp: Long,
    val level: Int,
    val temperature: Float,
    val currentNowMa: Float,
    val watts: Float,
)