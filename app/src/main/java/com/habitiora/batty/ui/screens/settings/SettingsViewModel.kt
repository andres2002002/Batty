package com.habitiora.batty.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitiora.batty.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
): ViewModel() {
    val isMonitoringEnabled = settingsRepository.isBatteryMonitorEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val isNotificationEnabled = settingsRepository.isBatteryNotificationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    fun toggleNotification(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBatteryNotificationEnabled(value)
        }
    }

    val isDNDEnabled = settingsRepository.isNotificationsDNDEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    fun toggleDND(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsDNDEnabled(value)
        }
    }

    val lowThreshold = settingsRepository.batteryThresholdsUnder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val highThreshold = settingsRepository.batteryThresholdsOver
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun addLowThreshold(threshold: Int) {
        viewModelScope.launch {
            settingsRepository.addBatteryThresholdsUnder(threshold)
        }
    }

    fun addHighThreshold(threshold: Int) {
        viewModelScope.launch {
            settingsRepository.addBatteryThresholdsOver(threshold)
        }
    }

    fun updateLowThreshold(oldThreshold: Int, newThreshold: Int) {
        viewModelScope.launch {
            settingsRepository.updateBatteryThresholdsUnder(oldThreshold, newThreshold)
        }
    }

    fun updateHighThreshold(oldThreshold: Int, newThreshold: Int) {
        viewModelScope.launch {
            settingsRepository.updateBatteryThresholdsOver(oldThreshold, newThreshold)
        }
    }

    fun removeLowThreshold(threshold: Int) {
        viewModelScope.launch {
            settingsRepository.removeBatteryThresholdsUnder(threshold)
        }
    }

    fun removeHighThreshold(threshold: Int) {
        viewModelScope.launch {
            settingsRepository.removeBatteryThresholdsOver(threshold)
        }
    }

}