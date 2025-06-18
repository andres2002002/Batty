package com.habitiora.batty.domain.repository

interface SettingsRepository {
    fun isBatteryMonitorEnabled(): Boolean
    fun setBatteryMonitorEnabled(enabled: Boolean)
    fun getBatteryLevelUnder(): Int
    fun setBatteryLevelUnder(level: Int)
    fun getBatteryLevelOver(): Int
    fun setBatteryLevelOver(level: Int)
}