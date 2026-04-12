package com.habitiora.batty.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitiora.batty.domain.model.ChartType
import com.habitiora.batty.domain.model.TimeRange
import com.habitiora.batty.domain.useCase.GetBatteryStatsUseCase
import com.habitiora.batty.domain.useCase.ObserveChartDataUseCase
import com.habitiora.batty.ui.utils.StatsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

@HiltViewModel
class BatteryHistoryViewModel @Inject constructor(
    private val getStatsUseCase: GetBatteryStatsUseCase,
    private val observeChartDataUseCase: ObserveChartDataUseCase,
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(TimeRange.LAST_24H)
    private val _selectedChart = MutableStateFlow(ChartType.LEVEL)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatsUiState> = combine(
        _selectedRange,
        _selectedChart
    ) { range, chart -> range to chart }
        .flatMapLatest { (range, chart) ->
            observeChartDataUseCase(range)
                .map { data ->
                    val stats = getStatsUseCase(range)
                    StatsUiState.Success(
                        stats         = stats,
                        chartData     = data,
                        selectedRange = range,
                        selectedChart = chart,
                    ) as StatsUiState
                }
                .onStart { emit(StatsUiState.Loading) }
                .catch { e ->
                    Timber.e(e, "Error loading stats for $range")
                    emit(StatsUiState.Error(e.message ?: "Error loading stats"))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsUiState.Loading
        )

    fun selectTimeRange(range: TimeRange) { _selectedRange.value = range }
    fun selectChartType(type: ChartType)  { _selectedChart.value = type }
}
