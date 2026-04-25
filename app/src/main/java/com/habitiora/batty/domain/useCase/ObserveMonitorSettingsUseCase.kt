package com.habitiora.batty.domain.useCase

import com.habitiora.batty.domain.model.MonitorSettings
import com.habitiora.batty.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMonitorSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<MonitorSettings> = repository.observe()
}