package com.habitiora.batty.domain.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import com.habitiora.batty.R

@Stable
enum class ChartType(@StringRes val labelRes: Int, val unit: String) {
    LEVEL(R.string.chart_level, "%"),
    TEMPERATURE(R.string.chart_temperature, "°C"),
    CURRENT(R.string.chart_current, "mA"),
}