package com.habitiora.batty.domain.repository

import com.habitiora.batty.domain.model.AlertPolicy
import com.habitiora.batty.domain.model.MonitorSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observe(): Flow<MonitorSettings>
    suspend fun get(): MonitorSettings
    suspend fun update(settings: MonitorSettings)

    // Helpers granulares para evitar read-modify-write desde la UI
    suspend fun setMonitorBattery(enabled: Boolean)
    suspend fun setAlertPolicy(alertPolicy: AlertPolicy)
    suspend fun setStartOnBoot(enabled: Boolean)
}