package com.habitiora.batty.ui.utils

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.habitiora.batty.domain.model.BatteryDataPoint
import com.habitiora.batty.domain.model.BatteryStats
import com.habitiora.batty.domain.model.ChartType
import com.habitiora.batty.domain.model.LastCycleStats
import com.habitiora.batty.domain.model.TimeRange

@Stable
sealed interface StatsUiState {
    data object Loading : StatsUiState

    @Immutable
    data class Success(
        val stats: BatteryStats,
        val chartData: List<BatteryDataPoint>,
        val selectedRange: TimeRange,
        val selectedChart: ChartType,
        val lastCycleStats: LastCycleStats?
    ) : StatsUiState {
        val hasData: Boolean get() = chartData.isNotEmpty()
    }

    @Immutable
    data class Error(val message: String) : StatsUiState
}