package com.habitiora.batty.domain.model

import androidx.annotation.StringRes
import com.habitiora.batty.R

enum class BatteryPlugged(@StringRes val labelRes: Int) {
    AC(R.string.status_ac_charger),
    USB(R.string.status_usb_charger),
    WIRELESS(R.string.status_wireless_charger),
    NONE(R.string.status_unplugged);
}