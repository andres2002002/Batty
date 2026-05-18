package com.habitiora.batty.ui.screens.history

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Battery4Bar
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.habitiora.batty.R
import com.habitiora.batty.domain.model.BatteryStats
import com.habitiora.batty.domain.model.ChartType
import com.habitiora.batty.domain.model.TimeRange
import com.habitiora.batty.ui.components.charts.BatteryChartByType
import com.habitiora.batty.ui.utils.StatsUiState

@Composable
fun BatteryHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: BatteryHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is StatsUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is StatsUiState.Error -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is StatsUiState.Success -> {
            StatsContent(
                state = state,
                onRangeSelected = { viewModel.selectTimeRange(it) },
                onChartSelected = { viewModel.selectChartType(it) },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun StatsContent(
    state: StatsUiState.Success,
    onRangeSelected: (TimeRange) -> Unit,
    onChartSelected: (ChartType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "Time Selector") {
            TimeRangeSelector(selected = state.selectedRange, onSelected = onRangeSelected)
        }

        if (!state.hasData) {
            item(key = "Empty State") {
                EmptyState()
            }
            return@LazyColumn
        }

        item(key = "Chart Card") {
            ChartCard(state = state, onChartSelected = onChartSelected)
        }

        item(key = "Overview Row") {
            OverviewRow(stats = state.stats)
        }

        item(key = "Level Stats Card") {
            LevelStatsCard(stats = state.stats)
        }

        item(key = "Temperature Stats Card") {
            TemperatureStatsCard(stats = state.stats)
        }

        item(key = "Power Stats Card") {
            PowerStatsCard(stats = state.stats)
        }

        item(key = "System Usage Card") {
            SystemUsageCard(stats = state.stats)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Time range
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TimeRangeSelector(
    selected: TimeRange,
    onSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        TimeRange.entries.forEachIndexed { index, range ->
            SegmentedButton(
                selected = selected == range,
                onClick = { onSelected(range) },
                shape = SegmentedButtonDefaults.itemShape(index, TimeRange.entries.size),
                label = { Text(stringResource(range.labelRes)) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chart card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChartCard(
    state: StatsUiState.Success,
    onChartSelected: (ChartType) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)) {

            ChartTypeSelector(
                selected = state.selectedChart,
                onSelected = onChartSelected
            )

            Spacer(Modifier.height(12.dp))

            AnimatedContent(
                targetState = state.selectedChart,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "chart_type_transition"
            ) { chartType ->
                BatteryChartByType(
                    type = chartType,
                    data = state.chartData,
                    modifier = Modifier.fillMaxWidth(),
                    height = 200.dp
                )
            }

            Spacer(Modifier.height(8.dp))
            ChartSeriesSummary(state = state)
        }
    }
}

@Composable
private fun ChartTypeSelector(
    selected: ChartType,
    onSelected: (ChartType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChartType.entries.forEach { type ->
            val isSelected = type == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(type) },
                label = { Text(stringResource(type.labelRes), style = MaterialTheme.typography.labelMedium) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
private fun ChartSeriesSummary(state: StatsUiState.Success) {
    val stats = state.stats
    val lastCycleStats = state.lastCycleStats

    val summaryData = remember(state.selectedChart, stats) {
        when (state.selectedChart) {
            ChartType.LEVEL -> Triple(
                "${stats.minLevel}%",
                "${"%.1f".format(stats.avgLevel)}%",
                "${stats.maxLevel}%"
            )
            ChartType.TEMPERATURE -> Triple(
                "${"%.1f".format(stats.minTemperature)}°C",
                "${"%.1f".format(stats.avgTemperature)}°C",
                "${"%.1f".format(stats.maxTemperature)}°C"
            )
            ChartType.CURRENT -> Triple(
                "—",
                "${"%.0f".format(stats.avgCurrentMa)} mA",
                "${"%.0f".format(stats.peakCurrentMa)} mA"
            )
        }
    }

    val (minLabel, avgLabel, maxLabel) = summaryData

    // Fusión de tiempos: "14:30 ➔ 16:00" o "14:30 ➔ Now" eliminando los segundos
    val cycleWindow = remember(lastCycleStats) {
        if (lastCycleStats == null) return@remember "--:--"
        val start = DateFormat.format("HH:mm", lastCycleStats.connectedAt)
        if (lastCycleStats.disconnectedAt != null) {
            val end = DateFormat.format("HH:mm", lastCycleStats.disconnectedAt)
            "$start \u2794 $end"
        } else {
            "$start \u2794 NOW_HOLDER"
        }
    }

    val nowStr = stringResource(R.string.history_time_now)
    val localizedCycleWindow = cycleWindow.replace("NOW_HOLDER", nowStr)

    val durationCharge = lastCycleStats?.durationConnectedMs?.let { ms ->
        val hours = ms / 3_600_000
        val minutes = (ms % 3_600_000) / 60_000
        if (hours > 0) {
            stringResource(R.string.history_duration_format_hm, hours, minutes)
        } else {
            stringResource(R.string.history_duration_format_m, minutes)
        }
    } ?: "--"

    val levelGained = remember(lastCycleStats?.levelGained) {
        lastCycleStats?.levelGained?.let { "+$it%" } ?: "--%"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección: Ciclo de Carga (3 Columnas con anchos asimétricos para evitar amontonamiento)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.history_last_charge_cycle),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    MiniStat(
                        label = stringResource(R.string.history_time_window),
                        value = localizedCycleWindow,
                        modifier = Modifier.weight(1.3f)
                    )
                    MiniStat(
                        label = stringResource(R.string.history_duration),
                        value = durationCharge,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStat(
                        label = stringResource(R.string.history_gain),
                        value = levelGained,
                        modifier = Modifier.weight(0.8f)
                    )
                }
            }

            // Sección: Métricas de la Gráfica (Mantenemos 4 columnas porque los datos son cortos)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.history_chart_series),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (state.selectedChart != ChartType.CURRENT) {
                        MiniStat(label = stringResource(R.string.history_min), value = minLabel, modifier = Modifier.weight(1f))
                    }
                    MiniStat(label = stringResource(R.string.history_avg), value = avgLabel, modifier = Modifier.weight(1f))
                    MiniStat(
                        label = if (state.selectedChart == ChartType.CURRENT) {
                            stringResource(R.string.history_peak)
                        } else {
                            stringResource(R.string.history_max)
                        },
                        value = maxLabel,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStat(label = stringResource(R.string.history_samples), value = "${stats.totalSamples}", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MiniStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Overview row — 3 KPIs
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OverviewRow(
    stats: BatteryStats,
    modifier: Modifier = Modifier
) {
    // Memoizamos los strings para optimizar recomposiciones
    val avgLevelStr = remember(stats.avgLevel) { "${"%.0f".format(stats.avgLevel)}%" }
    val avgTempStr = remember(stats.avgTemperature) { "${"%.1f".format(stats.avgTemperature)}°C" }
    val avgWattsStr = remember(stats.avgWatts) { "${"%.1f".format(stats.avgWatts)} W" }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        KpiCard(
            icon = Icons.Outlined.BatteryStd,
            label = stringResource(R.string.history_avg_level),
            value = avgLevelStr,
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            icon = Icons.Outlined.Thermostat,
            label = stringResource(R.string.history_avg_temp),
            value = avgTempStr,
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            icon = Icons.Outlined.ElectricBolt,
            label = stringResource(R.string.history_avg_power),
            value = avgWattsStr,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun KpiCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Detail cards
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LevelStatsCard(
    stats: BatteryStats,
    modifier: Modifier = Modifier
) {
    val avgLevel = remember(stats.avgLevel) { "${"%.1f".format(stats.avgLevel)}%" }

    StatsDetailCard(
        title = stringResource(R.string.history_battery_level_card),
        icon = Icons.Outlined.Battery4Bar,
        modifier = modifier
    ) {
        StatRow(stringResource(R.string.history_average), avgLevel)
        StatRow(stringResource(R.string.history_minimum), "${stats.minLevel}%")
        StatRow(stringResource(R.string.history_maximum), "${stats.maxLevel}%")
        StatRow(stringResource(R.string.history_variation), stringResource(R.string.history_variation_range, "${stats.levelRange}%"))
    }
}

@Composable
private fun TemperatureStatsCard(
    stats: BatteryStats,
    modifier: Modifier = Modifier
) {
    val avgTemp = remember(stats.avgTemperature) { "${"%.1f".format(stats.avgTemperature)}°C" }
    val minTemp = remember(stats.minTemperature) { "${"%.1f".format(stats.minTemperature)}°C" }
    val maxTemp = remember(stats.maxTemperature) { "${"%.1f".format(stats.maxTemperature)}°C" }
    val varTemp = remember(stats.temperatureRange) { "${"%.1f".format(stats.temperatureRange)}°C range" }

    StatsDetailCard(
        title = stringResource(R.string.dashboard_temperature_label),
        icon = Icons.Outlined.Thermostat,
        modifier = modifier
    ) {
        StatRow(stringResource(R.string.history_average), avgTemp)
        StatRow(stringResource(R.string.history_minimum), minTemp)
        StatRow(stringResource(R.string.history_maximum), maxTemp)
        StatRow(stringResource(R.string.history_variation), stringResource(R.string.history_variation_range, varTemp))
    }
}

@Composable
private fun PowerStatsCard(
    stats: BatteryStats,
    modifier: Modifier = Modifier
) {
    val avgCurrent = remember(stats.avgCurrentMa) { "${"%.0f".format(stats.avgCurrentMa)} mA" }
    val peakCurrent = remember(stats.peakCurrentMa) { "${"%.0f".format(stats.peakCurrentMa)} mA" }
    val avgWatts = remember(stats.avgWatts) { "${"%.2f".format(stats.avgWatts)} W" }
    val peakWatts = remember(stats.peakWatts) { "${"%.2f".format(stats.peakWatts)} W" }
    val avgVoltage = remember(stats.avgVoltage) { "${"%.0f".format(stats.avgVoltage)} mV" }

    StatsDetailCard(
        title = stringResource(R.string.history_power_current_card),
        icon = Icons.Outlined.ElectricBolt,
        modifier = modifier
    ) {
        StatRow(stringResource(R.string.history_avg_current), avgCurrent)
        StatRow(stringResource(R.string.history_peak_current), peakCurrent)
        StatRow(stringResource(R.string.history_avg_power), avgWatts)
        StatRow(stringResource(R.string.history_peak_power), peakWatts)
        StatRow(stringResource(R.string.history_avg_voltage), avgVoltage)
    }
}

@Composable
private fun SystemUsageCard(
    stats: BatteryStats,
    modifier: Modifier = Modifier
) {
    val screenOnPct = remember(stats.screenOnRatio) { (stats.screenOnRatio * 100).toInt() }
    val batterySaverPct = remember(stats.batterySaverRatio) { (stats.batterySaverRatio * 100).toInt() }

    StatsDetailCard(
        title = stringResource(R.string.history_system_usage_card),
        icon = Icons.Outlined.PhoneAndroid,
        modifier = modifier
    ) {
        StatRow(stringResource(R.string.history_screen_on), stringResource(R.string.history_samples_suffix, screenOnPct))
        UsageBar(
            ratio = stats.screenOnRatio,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))

        StatRow(stringResource(R.string.history_battery_saver), stringResource(R.string.history_samples_suffix, batterySaverPct))
        UsageBar(
            ratio = stats.batterySaverRatio,
            color = MaterialTheme.colorScheme.tertiary
        )

        Spacer(Modifier.height(4.dp))
        StatRow(stringResource(R.string.history_total_snapshots), "${stats.totalSamples}")
    }
}

@Composable
private fun UsageBar(
    ratio: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = { ratio.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .padding(top = 2.dp),
        color = color,
        trackColor = color.copy(alpha = 0.12f)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared primitives
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatsDetailCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
            content()
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Outlined.BarChart,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Text(
                stringResource(R.string.history_no_data_period),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.history_enable_monitoring_msg),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}