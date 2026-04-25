package com.habitiora.batty.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitiora.batty.domain.controller.BatteryServiceController
import com.habitiora.batty.domain.model.ConnectionState
import com.habitiora.batty.domain.model.ServiceErrorCause
import com.habitiora.batty.domain.model.ServiceState
import com.habitiora.batty.domain.useCase.ObserveLiveBatteryUseCase
import com.habitiora.batty.domain.useCase.ObserveMonitorSettingsUseCase
import com.habitiora.batty.domain.useCase.UpdateMonitorSettingsUseCase
import com.habitiora.batty.ui.utils.BatteryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeLiveBatteryUseCase: ObserveLiveBatteryUseCase,
    private val observeMonitorSettingsUseCase: ObserveMonitorSettingsUseCase,
    private val updateMonitorSettingsUseCase: UpdateMonitorSettingsUseCase,
    private val batteryServiceController: BatteryServiceController,
) : ViewModel() {

    init {
        syncInitialServiceState()
    }

    private fun syncInitialServiceState() {
        viewModelScope.launch {
            runCatching {
                // Leemos síncronamente el estado real desde DataStore en el primer frame
                val isMonitorEnabled = observeMonitorSettingsUseCase().first().monitorBattery

                if (isMonitorEnabled) {
                    Timber.d("Sync: Monitor is enabled, ensuring service is started and bound")
                    batteryServiceController.startServiceAndBind()
                } else {
                    Timber.d("Sync: Monitor is disabled, ensuring unbound state")
                    batteryServiceController.unbindOnly()
                }
            }.onFailure { Timber.e(it, "Error syncing initial service state") }
        }
    }

    val uiState: StateFlow<BatteryUiState> = combine(
        observeLiveBatteryUseCase(),
        observeMonitorSettingsUseCase().map { it.monitorBattery }.distinctUntilChanged(),
        batteryServiceController.connectionState
    ) { info, monitorBattery, connection ->
        BatteryUiState.Success(
            liveInfo = info,
            serviceState = resolveServiceState(monitorBattery, connection)
        ) as BatteryUiState
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

    fun setMonitorBattery(enabled: Boolean) {
        viewModelScope.launch {
            runCatching {
                updateMonitorSettingsUseCase.setMonitorBattery(enabled)
                if (enabled) {
                    batteryServiceController.startServiceAndBind()
                } else {
                    batteryServiceController.stopServiceAndUnbind()
                }
            }.onFailure { Timber.e(it, "Error toggling monitorBattery") }
        }
    }

    fun retryService() {
        batteryServiceController.startServiceAndBind()
    }

    private fun resolveServiceState(
        monitorBattery: Boolean,
        connection: ConnectionState
    ): ServiceState = when {
        !monitorBattery -> ServiceState.Inactive
        connection == ConnectionState.Connected -> ServiceState.Active
        connection == ConnectionState.Idle || connection == ConnectionState.Connecting -> ServiceState.Loading
        connection == ConnectionState.Disconnected -> ServiceState.Error(ServiceErrorCause.UNEXPECTED_DISCONNECT)
        connection == ConnectionState.Unavailable -> ServiceState.Error(ServiceErrorCause.BIND_FAILED)
        else -> ServiceState.Loading
    }
}