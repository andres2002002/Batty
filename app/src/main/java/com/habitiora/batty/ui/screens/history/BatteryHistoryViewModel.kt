package com.habitiora.batty.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitiora.batty.data.repository.BatteryEntityRepository
import com.habitiora.batty.domain.model.BatteryState
import com.habitiora.batty.ui.components.charts.line.BatteryChartConfig
import com.habitiora.batty.ui.components.charts.line.BatteryMetric
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class BatteryHistoryViewModel @Inject constructor(
    private val batteryRepository: BatteryEntityRepository
) : ViewModel() {
    companion object{
        private const val ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000
    }
    private val currentDayInMills = System.currentTimeMillis()
    val batteryHistory = batteryRepository.getBatteryStatesSince(currentDayInMills - ONE_DAY_IN_MILLIS)
        .map { fullList ->
            reduceBatteryStatesByInterval(fullList)
        }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun reduceBatteryStatesByInterval(
        states: List<BatteryState>,
        intervalMillis: Long = 5 * 60 * 1000 // 5 minutos por defecto
    ): List<BatteryState> {
        if (states.isEmpty()) return emptyList()

        val grouped = states
            .sortedBy { it.timestamp }
            .groupBy { it.timestamp / intervalMillis }

        return grouped.mapNotNull { (_, group) ->
            if (group.isEmpty()) return@mapNotNull null

            val avgLevel = group.map { it.batteryLevel }.average().toInt()
            val avgTimestamp = group.map { it.timestamp }.average().toLong()
            val mostFrequentCharging = group.groupingBy { it.isCharging }.eachCount().maxByOrNull { it.value }?.key ?: false

            group.first().copy(
                timestamp = avgTimestamp,
                batteryLevel = avgLevel,
                isCharging = mostFrequentCharging
            )
        }
    }

}

// Configuraciones preestablecidas para diferentes casos de uso
object BatteryChartConfigs {
    val BASIC_LEVEL = BatteryChartConfig(
        metrics = listOf(BatteryMetric.BATTERY_LEVEL),
        timeFormat = "HH:mm",
        maxDataPoints = 100
    )

    val DETAILED_MONITORING = BatteryChartConfig(
        metrics = listOf(
            BatteryMetric.BATTERY_LEVEL,
            BatteryMetric.TEMPERATURE,
            BatteryMetric.VOLTAGE
        ),
        timeFormat = "HH:mm",
        showCharging = true,
        showTemperature = true,
        maxDataPoints = 200
    )

    val CHARGING_ANALYSIS = BatteryChartConfig(
        metrics = listOf(
            BatteryMetric.BATTERY_LEVEL,
            BatteryMetric.CHARGING_RATE,
            BatteryMetric.CURRENT
        ),
        timeFormat = "HH:mm",
        showCharging = true,
        maxDataPoints = 50
    )
}