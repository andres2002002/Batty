package com.habitiora.batty.domain.model

import androidx.annotation.StringRes
import com.habitiora.batty.R

enum class ChargingType(@StringRes val nameId: Int) {
    NOT_CHARGING(R.string.status_not_charging),
    AC_CHARGER(R.string.status_ac_charger),        // Cargador de pared
    USB_CHARGER(R.string.status_usb_charger),       // USB (más lento)
    WIRELESS_CHARGER(R.string.status_wireless_charger),  // Carga inalámbrica
    UNKNOWN(R.string.status_unknown)
}