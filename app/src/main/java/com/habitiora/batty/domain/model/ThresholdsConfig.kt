package com.habitiora.batty.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class ThresholdsConfig(
    val lowThresholds: List<Int> = listOf(20, 15, 10, 4),
    val highThresholds: List<Int> = listOf(80, 100),
    val triggeredLevel: Int = 0
){
    val criticalLevel: Int get() = lowThresholds.minOrNull() ?: 4

    fun isCriticalLevel(level: Int): Boolean = level <= criticalLevel
    fun isVeryLowLevel(level: Int): Boolean =
        lowThresholds.sortedDescending().getOrNull(1)?.let { level <= it } ?: false
}