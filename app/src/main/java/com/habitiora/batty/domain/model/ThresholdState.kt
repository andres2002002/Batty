package com.habitiora.batty.domain.model

import androidx.compose.runtime.Stable


@Stable
data class ThresholdState(
    val hasActiveLowTrigger: Boolean = false,
    val hasActiveHighTrigger: Boolean = false,
    val activeLevel: Int = -1
) {
    val hasActiveTrigger: Boolean get() = hasActiveLowTrigger || hasActiveHighTrigger
    val isHighTriggerActive: Boolean get() = hasActiveHighTrigger
}