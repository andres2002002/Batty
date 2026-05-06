package com.habitiora.batty.data.local.room.pojo

import androidx.compose.runtime.Immutable

@Immutable
data class BatteryDataPointEntity(
    val timestamp: Long,
    val level: Int,
    val temperature: Float,
    val currentNowMa: Float,
    val watts: Float,
    val isCharging: Boolean // ⚠️ Requerido
)