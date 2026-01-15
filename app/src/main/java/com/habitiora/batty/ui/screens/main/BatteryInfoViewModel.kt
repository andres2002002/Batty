package com.habitiora.batty.ui.screens.main

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitiora.batty.domain.model.BatteryState
import com.habitiora.batty.domain.repository.SettingsRepository
import com.habitiora.batty.services.BatteryForegroundService
import com.habitiora.batty.services.BatteryReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BatteryInfoViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val batteryReceiver: BatteryReceiver,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _batteryState = MutableStateFlow<BatteryState?>(null)
    val batteryState: StateFlow<BatteryState?> = _batteryState.asStateFlow()

    val isMonitoring: StateFlow<Boolean> = settingsRepository.isBatteryMonitorEnabled
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun toggleMonitoring(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBatteryMonitorEnabled(value)
        }
    }

    private fun updateBatteryState(newState: BatteryState) {
        _batteryState.value = newState
    }

    init {
        initialize()
        monitoringListener()
    }
    private fun initialize(){
        viewModelScope.launch {
            try {
                Timber.d("Initializing ViewModel")
                batteryStateListener()
                Timber.d("Battery state listener initialized")
            } catch (e: Exception) {
                Timber.e(e, "Error initializing ViewModel")
            }
        }
    }

    private suspend fun batteryStateListener() {
        try {
            batteryReceiver.batteryState.collect { state ->
                state?.let { updateBatteryState(it) }
                Timber.d("Battery state: $state")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error listening to battery state")
        }
    }


    fun startBatteryMonitorService(context: Context) {
        val serviceIntent = Intent(context, BatteryForegroundService::class.java)
        context.startForegroundService(serviceIntent)
    }

    private fun monitoringListener() {
        viewModelScope.launch {
            settingsRepository.isBatteryMonitorEnabled.collect { isMonitoring ->
                if (!isMonitoring) {
                    stopBatteryMonitorService(context)
                }
            }
        }
    }

    private fun stopBatteryMonitorService(context: Context) {
        val stopIntent = Intent(context, BatteryForegroundService::class.java).apply {
            action = BatteryForegroundService.ACTION_STOP_MONITORING
        }
        context.startService(stopIntent)
    }
}