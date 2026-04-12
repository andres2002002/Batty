package com.habitiora.batty.ui.screens.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitiora.batty.domain.model.ServiceState
import com.habitiora.batty.domain.useCase.ObserveLiveBatteryUseCase
import com.habitiora.batty.domain.useCase.ObserveMonitorSettingsUseCase
import com.habitiora.batty.domain.useCase.UpdateMonitorSettingsUseCase
import com.habitiora.batty.services.BatteryMonitorService
import com.habitiora.batty.ui.utils.BatteryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BatteryInfoViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    observeLiveBatteryUseCase: ObserveLiveBatteryUseCase,
    private val observeMonitorSettingsUseCase: ObserveMonitorSettingsUseCase,
    private val updateMonitorSettingsUseCase: UpdateMonitorSettingsUseCase,
) : ViewModel() {
    val uiState: StateFlow<BatteryUiState> = observeLiveBatteryUseCase()
        .combine(observeMonitorSettingsUseCase()) { info, settings ->
            BatteryUiState.Success(liveInfo = info, serviceState = ServiceState.Active) as BatteryUiState
        }
        .catch { e ->
            Timber.e(e, "Error observing live battery")
            emit(BatteryUiState.Error(e.message ?: "Unexpected error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BatteryUiState.Loading
        )

    fun toggleMonitoring(value: Boolean) {
        viewModelScope.launch {
            updateMonitorSettingsUseCase.setMonitorBattery(value)
        }
    }

    init {
        monitoringListener()
    }

    private fun monitoringListener() {
        viewModelScope.launch {
            observeMonitorSettingsUseCase().collect { state ->
                if (!state.monitorBattery) {
                    stopService()
                }
            }
        }
    }

    fun startService() {
        context.startForegroundService(BatteryMonitorService.startIntent(context))
    }

    fun stopService() {
        context.startService(BatteryMonitorService.stopIntent(context))
    }
}