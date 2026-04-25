package com.habitiora.batty.data.repository

import androidx.datastore.core.DataStore
import com.habitiora.batty.domain.model.AlertPolicy
import com.habitiora.batty.domain.model.MonitorSettings
import com.habitiora.batty.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<MonitorSettings>
): SettingsRepository {
    override fun observe(): Flow<MonitorSettings> = dataStore.data

    override suspend fun get(): MonitorSettings = observe().first()

    override suspend fun update(settings: MonitorSettings) {
        dataStore.updateData { settings }
    }

    override suspend fun setMonitorBattery(enabled: Boolean) {
        dataStore.updateData { settings ->
            settings.copy(monitorBattery = enabled)
        }
    }
    override suspend fun setAlertPolicy(alertPolicy: AlertPolicy) {
        dataStore.updateData { settings ->
            settings.copy(alertPolicy = alertPolicy)
        }
    }

    override suspend fun setStartOnBoot(enabled: Boolean) {
        dataStore.updateData { settings ->
            settings.copy(startOnBoot = enabled)
        }
    }
}