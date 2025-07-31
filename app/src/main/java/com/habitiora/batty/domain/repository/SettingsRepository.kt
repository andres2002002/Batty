package com.habitiora.batty.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val isBatteryMonitorEnabled: Flow<Boolean>
    suspend fun setBatteryMonitorEnabled(enabled: Boolean): Boolean

    val isBatteryNotificationEnabled: Flow<Boolean>
    suspend fun setBatteryNotificationEnabled(enabled: Boolean): Boolean

    val isNotificationsDNDEnabled: Flow<Boolean>
    suspend fun setNotificationsDNDEnabled(enabled: Boolean): Boolean

    val batteryThresholdsUnder: Flow<List<Int>>
    suspend fun addBatteryThresholdsUnder(level: Int): Boolean
    suspend fun updateBatteryThresholdsUnder(currentLevel: Int, newLevel: Int): Boolean
    suspend fun removeBatteryThresholdsUnder(level: Int): Boolean
    val batteryThresholdsOver: Flow<List<Int>>
    suspend fun addBatteryThresholdsOver(level: Int): Boolean
    suspend fun updateBatteryThresholdsOver(currentLevel: Int, newLevel: Int): Boolean
    suspend fun removeBatteryThresholdsOver(level: Int): Boolean

}