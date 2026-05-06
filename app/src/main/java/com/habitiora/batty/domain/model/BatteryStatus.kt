package com.habitiora.batty.domain.model

enum class BatteryStatus(val label: String) {
    CHARGING("Charging"),
    DISCHARGING("Discharging"),
    FULL("Full"),
    NOT_CHARGING("Not Charging"),
    UNKNOWN("Unknown");

    companion object{
        fun fromString(value: String): BatteryStatus {
            return entries.find { it.label == value } ?: UNKNOWN
        }
    }
}