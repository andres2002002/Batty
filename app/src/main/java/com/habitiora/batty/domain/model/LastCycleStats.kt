package com.habitiora.batty.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class LastCycleStats(
    val connectedAt: Long,
    val disconnectedAt: Long?,
    val durationConnectedMs: Long,
    val levelGained: Int
)