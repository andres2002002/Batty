package com.habitiora.batty.domain.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import com.habitiora.batty.R

@Stable
enum class TimeRange(val hours: Int, @StringRes val labelRes: Int) {
    LAST_1H(1, R.string.time_range_1h),
    LAST_6H(6, R.string.time_range_6h),
    LAST_24H(24, R.string.time_range_24h),
    LAST_7D(168, R.string.time_range_7d),
    LAST_30D(720, R.string.time_range_30d)
}