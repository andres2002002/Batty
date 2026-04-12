package com.habitiora.batty.ui.utils

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.habitiora.batty.domain.model.BatteryInfo
import com.habitiora.batty.domain.model.BatteryStats
import com.habitiora.batty.domain.model.LevelDataPoint
import com.habitiora.batty.domain.model.ServiceState
import com.habitiora.batty.domain.model.TimeRange

@Stable
sealed interface BatteryUiState {
    data object Loading : BatteryUiState

    @Immutable
    data class Success(
        val liveInfo: BatteryInfo,
        val serviceState: ServiceState,
    ) : BatteryUiState {
        /** Shortcut para la UI — evita when() en cada punto de acceso */
        val isMonitoring: Boolean get() = serviceState == ServiceState.Active
    }

    @Immutable
    data class Error(val message: String) : BatteryUiState
}