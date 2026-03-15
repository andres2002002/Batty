package com.habitiora.batty.data.datastore

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class ThresholdsConfig(
    val lowThresholds: List<Int> = listOf(15, 10, 4),
    val highThresholds: List<Int> = listOf(80, 100),
    val triggeredLevel: Int = 0
)