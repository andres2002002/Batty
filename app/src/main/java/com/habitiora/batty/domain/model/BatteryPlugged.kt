package com.habitiora.batty.domain.model

enum class BatteryPlugged(val label: String) {
    AC("AC Power"),
    USB("USB"),
    WIRELESS("Wireless"),
    NONE("Unplugged")
}