package com.habitiora.batty.ui.screens.settings

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitiora.batty.domain.model.AlertPolicy
import com.habitiora.batty.domain.repository.SettingsRepository
import com.habitiora.batty.domain.useCase.ObserveMonitorSettingsUseCase
import com.habitiora.batty.domain.useCase.ObserveThresholdsConfigUseCase
import com.habitiora.batty.domain.useCase.UpdateMonitorSettingsUseCase
import com.habitiora.batty.domain.useCase.UpdateThresholdsConfigUseCase
import com.habitiora.batty.services.NotificationHelper
import com.habitiora.batty.ui.utils.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeMonitorSettingsUseCase: ObserveMonitorSettingsUseCase,
    private val updateMonitorSettingsUseCase: UpdateMonitorSettingsUseCase,
    observeThresholdsConfigUseCase: ObserveThresholdsConfigUseCase,
    private val updateThresholdsConfigUseCase: UpdateThresholdsConfigUseCase,
    private val notificationHelper: NotificationHelper,
): ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        observeMonitorSettingsUseCase(),
        observeThresholdsConfigUseCase()
    ) { settings, thresholds ->
        SettingsUiState.Success(
            settings = settings,
            thresholds = thresholds,
            dndBypass   = notificationHelper.getDndBypassState()
        ) as SettingsUiState
    }.catch { e ->
        Timber.e(e, "Error observing settings")
        emit(SettingsUiState.Loading) // Feedback real al usuario
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState.Loading
    )
    fun setAlertPolicy(alertPolicy: AlertPolicy) = launch {
        updateMonitorSettingsUseCase.setAlertPolicy(alertPolicy)
    }

    fun setStartOnBoot(enabled: Boolean) = launch {
        updateMonitorSettingsUseCase.setStartOnBoot(enabled)
    }
    fun addLowThreshold(threshold: Int) = launch {
        updateThresholdsConfigUseCase.addLowThreshold(threshold)
    }

    fun addHighThreshold(threshold: Int) = launch {
        updateThresholdsConfigUseCase.addHighThreshold(threshold)
    }

    fun updateLowThreshold(oldThreshold: Int, newThreshold: Int) = launch {
        updateThresholdsConfigUseCase.updateLowThreshold(oldThreshold, newThreshold)
    }

    fun updateHighThreshold(oldThreshold: Int, newThreshold: Int) = launch {
        updateThresholdsConfigUseCase.updateHighThreshold(oldThreshold, newThreshold)
    }

    fun removeLowThreshold(threshold: Int) = launch {
        updateThresholdsConfigUseCase.removeLowThreshold(threshold)
    }

    fun removeHighThreshold(threshold: Int) = launch {
        updateThresholdsConfigUseCase.removeHighThreshold(threshold)
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onFailure { Timber.e(it, "Error updating setting") }
        }
    }
}