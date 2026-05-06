package com.habitiora.batty.domain.model

enum class BatteryPlugged(val label: String) {
    AC("AC Power"),
    USB("USB"),
    WIRELESS("Wireless"),
    NONE("Unplugged");

    companion object{
        fun fromString(value: String): BatteryPlugged {
            return entries.find { it.label == value } ?: NONE
        }
    }
}