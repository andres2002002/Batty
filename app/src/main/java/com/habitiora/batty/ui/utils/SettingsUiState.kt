package com.habitiora.batty.ui.utils

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.habitiora.batty.domain.model.DndBypassState
import com.habitiora.batty.domain.model.MonitorSettings
import com.habitiora.batty.domain.model.ThresholdsConfig

@Stable
sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    @Immutable
    data class Success(
        val settings: MonitorSettings,
        val thresholds: ThresholdsConfig,
        val dndBypass: DndBypassState,
    ) : SettingsUiState
}