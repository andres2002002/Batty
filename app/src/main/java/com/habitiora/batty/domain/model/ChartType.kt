package com.habitiora.batty.domain.model

import androidx.compose.runtime.Stable

@Stable
enum class ChartType(val label: String, val unit: String) {
    LEVEL("Level", "%"),
    TEMPERATURE("Temperature", "°C"),
    CURRENT("Current", "mA"),
}