package com.habitiora.batty.domain.model

import androidx.annotation.StringRes
import com.habitiora.batty.R

enum class BatteryStatus(@StringRes val labelRes: Int) {
    CHARGING(R.string.status_charging),
    DISCHARGING(R.string.status_discharging),
    FULL(R.string.status_full),
    NOT_CHARGING(R.string.status_not_charging),
    UNKNOWN(R.string.status_unknown);
}