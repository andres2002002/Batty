package com.habitiora.batty.domain.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ThresholdEvent {
    val level: Int

    @Immutable
    data class LowBattery(
        override val level: Int,
        val isCritical: Boolean,
        val isVeryLow: Boolean
    ) : ThresholdEvent

    @Immutable
    data class HighBattery(
        override val level: Int,
        val isFullyCharged: Boolean
    ) : ThresholdEvent
}