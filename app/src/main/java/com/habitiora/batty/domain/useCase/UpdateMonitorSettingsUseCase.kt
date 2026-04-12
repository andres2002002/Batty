package com.habitiora.batty.domain.useCase

import com.habitiora.batty.domain.model.AlertPolicy
import com.habitiora.batty.domain.model.MonitorSettings
import com.habitiora.batty.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateMonitorSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(settings: MonitorSettings) = repository.update(settings)
    suspend fun setMonitorBattery(enabled: Boolean) = repository.setMonitorBattery(enabled)
    suspend fun setAlertPolicy(alertPolicy: AlertPolicy) = repository.setAlertPolicy(alertPolicy)
    suspend fun setStartOnBoot(enabled: Boolean) = repository.setStartOnBoot(enabled)
}