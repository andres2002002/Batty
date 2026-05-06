package com.habitiora.batty.domain.model

import androidx.compose.runtime.Stable

@Stable
enum class TimeRange(val hours: Int, val label: String) {
    LAST_1H(1, "1h"),
    LAST_6H(6, "6h"),
    LAST_24H(24, "24h"),
    LAST_7D(168, "7d"),
    LAST_30D(720, "30d")
}