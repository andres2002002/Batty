package com.habitiora.batty.domain.model

import androidx.annotation.StringRes
import com.habitiora.batty.R

enum class BatteryHealth(@StringRes val nameId: Int) {
    UNKNOWN(R.string.health_unknown),
    GOOD(R.string.health_good),
    OVERHEAT(R.string.health_overheat),
    DEAD(R.string.health_dead),
    OVER_VOLTAGE(R.string.health_over_voltage),
    UNSPECIFIED_FAILURE(R.string.health_unspecified_failure),
    COLD(R.string.health_cold)
}