package com.habitiora.batty.ui.screens.history

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Battery4Bar
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.habitiora.batty.R
import com.habitiora.batty.domain.model.BatteryState
import com.habitiora.batty.domain.model.BatteryStats
import com.habitiora.batty.domain.model.ChartType
import com.habitiora.batty.domain.model.TimeRange
import com.habitiora.batty.ui.components.charts.BatteryChartByType
import com.habitiora.batty.ui.components.charts.line.BatteryChartConfig
import com.habitiora.batty.ui.components.charts.line.BatteryHistoryLineChart
import com.habitiora.batty.ui.components.charts.line.BatteryMetric
import com.habitiora.batty.ui.utils.StatsUiState
import com.habitiora.batty.utils.toDecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BatteryHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: BatteryHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is StatsUiState.Loading -> CircularProgressIndicator()

        is StatsUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)

        is StatsUiState.Success -> StatsContent(
            state = state,
            onRangeSelected = viewModel::selectTimeRange,
            onChartSelected = viewModel::selectChartType,
            modifier = modifier
        )
    }
}


@Composable
private fun StatsContent(
    state: StatsUiState.Success,
    onRangeSelected: (TimeRange) -> Unit,
    onChartSelected: (ChartType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Time range ─────────────────────────────────────────────────
        TimeRangeSelector(selected = state.selectedRange, onSelected = onRangeSelected)

        if (!state.hasData) {
            EmptyState()
            return@Column
        }

        // ── Chart ──────────────────────────────────────────────────────
        ChartCard(state = state, onChartSelected = onChartSelected)

        // ── Overview ───────────────────────────────────────────────────
        OverviewRow(stats = state.stats)

        // ── Detail cards ───────────────────────────────────────────────
        LevelStatsCard(stats = state.stats)
        TemperatureStatsCard(stats = state.stats)
        PowerStatsCard(stats = state.stats)
        SystemUsageCard(stats = state.stats)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Time range
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TimeRangeSelector(selected: TimeRange, onSelected: (TimeRange) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        TimeRange.entries.forEachIndexed { index, range ->
            SegmentedButton(
                selected  = selected == range,
                onClick   = { onSelected(range) },
                shape     = SegmentedButtonDefaults.itemShape(index, TimeRange.entries.size),
                label     = { Text(range.label) }
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
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)) {

            // Chart type selector — tabs compactos
            ChartTypeSelector(
                selected    = state.selectedChart,
                onSelected  = onChartSelected
            )

            Spacer(Modifier.height(12.dp))

            // Animated crossfade entre series
            AnimatedContent(
                targetState = state.selectedChart,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "chart_type_transition"
            ) { chartType ->
                BatteryChartByType(
                    type     = chartType,
                    data     = state.chartData,
                    modifier = Modifier.fillMaxWidth(),
                    height   = 200.dp
                )
            }

            // Summary de la serie actual bajo el chart
            Spacer(Modifier.height(8.dp))
            ChartSeriesSummary(state = state)
        }
    }
}

@Composable
private fun ChartTypeSelector(selected: ChartType, onSelected: (ChartType) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChartType.entries.forEach { type ->
            val isSelected = type == selected
            FilterChip(
                selected = isSelected,
                onClick  = { onSelected(type) },
                label    = { Text(type.label, style = MaterialTheme.typography.labelMedium) },
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
    val (minLabel, avgLabel, maxLabel) = when (state.selectedChart) {
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

    HorizontalDivider()
    Spacer(Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if (state.selectedChart != ChartType.CURRENT) {
            MiniStat(label = "Min", value = minLabel)
        }
        MiniStat(label = "Avg", value = avgLabel)
        MiniStat(label = if (state.selectedChart == ChartType.CURRENT) "Peak" else "Max", value = maxLabel)
        MiniStat(label = "Samples", value = "${stats.totalSamples}")
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Overview row — 3 KPIs
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OverviewRow(stats: BatteryStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        KpiCard(
            icon    = Icons.Outlined.BatteryStd,
            label   = "Avg Level",
            value   = "${"%.0f".format(stats.avgLevel)}%",
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            icon    = Icons.Outlined.Thermostat,
            label   = "Avg Temp",
            value   = "${"%.1f".format(stats.avgTemperature)}°C",
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            icon    = Icons.Outlined.ElectricBolt,
            label   = "Avg Power",
            value   = "${"%.1f".format(stats.avgWatts)} W",
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
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Detail cards
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LevelStatsCard(stats: BatteryStats) {
    StatsDetailCard(
        title = "Battery Level",
        icon  = Icons.Outlined.Battery4Bar,
    ) {
        StatRow("Average", "${"%.1f".format(stats.avgLevel)}%")
        StatRow("Minimum", "${stats.minLevel}%")
        StatRow("Maximum", "${stats.maxLevel}%")
        StatRow("Variation", "${stats.levelRange}% range")
    }
}

@Composable
private fun TemperatureStatsCard(stats: BatteryStats) {
    StatsDetailCard(
        title = "Temperature",
        icon  = Icons.Outlined.Thermostat,
    ) {
        StatRow("Average", "${"%.1f".format(stats.avgTemperature)}°C")
        StatRow("Minimum", "${"%.1f".format(stats.minTemperature)}°C")
        StatRow("Maximum", "${"%.1f".format(stats.maxTemperature)}°C")
        StatRow("Variation", "${"%.1f".format(stats.temperatureRange)}°C range")
    }
}

@Composable
private fun PowerStatsCard(stats: BatteryStats) {
    StatsDetailCard(
        title = "Power & Current",
        icon  = Icons.Outlined.ElectricBolt,
    ) {
        StatRow("Avg current", "${"%.0f".format(stats.avgCurrentMa)} mA")
        StatRow("Peak current", "${"%.0f".format(stats.peakCurrentMa)} mA")
        StatRow("Avg power", "${"%.2f".format(stats.avgWatts)} W")
        StatRow("Peak power", "${"%.2f".format(stats.peakWatts)} W")
        StatRow("Avg voltage", "${"%.0f".format(stats.avgVoltage)} mV")
    }
}

@Composable
private fun SystemUsageCard(stats: BatteryStats) {
    StatsDetailCard(
        title = "System Usage",
        icon  = Icons.Outlined.PhoneAndroid,
    ) {
        val screenOnPct     = (stats.screenOnRatio * 100).toInt()
        val batterySaverPct = (stats.batterySaverRatio * 100).toInt()

        StatRow("Screen on", "$screenOnPct% of samples")
        UsageBar(
            ratio = stats.screenOnRatio,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))

        StatRow("Battery saver", "$batterySaverPct% of samples")
        UsageBar(
            ratio = stats.batterySaverRatio,
            color = MaterialTheme.colorScheme.tertiary
        )

        Spacer(Modifier.height(4.dp))
        StatRow("Total snapshots", "${stats.totalSamples}")
    }
}

@Composable
private fun UsageBar(ratio: Float, color: androidx.compose.ui.graphics.Color) {
    LinearProgressIndicator(
        progress    = { ratio.coerceIn(0f, 1f) },
        modifier    = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .padding(top = 2.dp),
        color       = color,
        trackColor  = color.copy(alpha = 0.12f)
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
                Icon(icon, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp))
                Text(title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
            content()
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
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
                "No data for this period",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Enable monitoring to start collecting data",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
/*@Composable
private fun BatteryHistoryContent(
    batteryHistory: List<BatteryState>,
    selectedMetrics: List<BatteryMetric>,
    isChartExpanded: Boolean,
){
    val batteryHistory by viewModel.batteryHistory.collectAsState()
    var selectedBatteryState by remember { mutableStateOf<BatteryState?>(null) }
    var selectedMetrics by remember {
        mutableStateOf(listOf(BatteryMetric.BATTERY_LEVEL))
    }
    var isChartExpanded by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.surface,
                        colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            ),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            HeaderSection(
                totalDataPoints = batteryHistory.size,
                dateRange = getDateRange(batteryHistory)?:stringResource(R.string.no_data)
            )
        }

        item {
            MetricSelectorCard(
                selectedMetrics = selectedMetrics,
                onMetricsChanged = { selectedMetrics = it }
            )
        }

        item {
            ChartSection(
                batteryHistory = batteryHistory,
                selectedMetrics = selectedMetrics,
                isExpanded = isChartExpanded,
                onExpandToggle = { isChartExpanded = !isChartExpanded },
                onDataPointSelected = { selectedBatteryState = it }
            )
        }

        item {
            AnimatedVisibility(
                visible = selectedBatteryState != null,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                selectedBatteryState?.let { state ->
                    BatteryStateDetailsCard(
                        batteryState = state,
                        onDismiss = { selectedBatteryState = null }
                    )
                }
            }
        }

        if (batteryHistory.isNotEmpty()) {
            item {
                StatisticsSummaryCard(batteryHistory = batteryHistory)
            }
        }
    }
}*/

@Composable
private fun HeaderSection(
    totalDataPoints: Int,
    dateRange: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.outline_analytics_24),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.history_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$totalDataPoints " + stringResource(R.string.history_registers_count) + " • $dateRange",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun MetricSelectorCard(
    selectedMetrics: List<BatteryMetric>,
    onMetricsChanged: (List<BatteryMetric>) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.round_tune_24),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.history_metrics_to_display),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(BatteryMetric.entries) { metric ->
                    MetricChip(
                        metric = metric,
                        isSelected = selectedMetrics.contains(metric),
                        onClick = {
                            val newMetrics = if (selectedMetrics.contains(metric)) {
                                selectedMetrics - metric
                            } else {
                                selectedMetrics + metric
                            }
                            onMetricsChanged(newMetrics.takeIf { it.isNotEmpty() } ?: listOf(metric))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricChip(
    metric: BatteryMetric,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val icon = getMetricIcon(metric)
    val color = getMetricColor(metric)

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isSelected) colorScheme.onPrimary else color
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(metric.labelId),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White
        ),
        border = if (!isSelected) {
            FilterChipDefaults.filterChipBorder(
                borderColor = color,
                selectedBorderColor = color,
                enabled = true,
                selected = isSelected
            )
        } else null
    )
}

@Composable
private fun ChartSection(
    batteryHistory: List<BatteryState>,
    selectedMetrics: List<BatteryMetric>,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onDataPointSelected: (BatteryState?) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val chartHeight by animateDpAsState(
        targetValue = if (isExpanded) 450.dp else 300.dp,
        animationSpec = tween(300),
        label = "chart_height"
    )
    val chartIcon = remember(isExpanded) {
        if (isExpanded) R.drawable.round_unfold_less_24 else R.drawable.round_unfold_more_24
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .animateContentSize(
                animationSpec = tween(300)
            ),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.round_show_chart_24),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.history_trends_chart_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colorScheme.onSurface
                    )
                }

                FilledTonalIconButton(
                    onClick = onExpandToggle,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = chartIcon),
                        contentDescription = stringResource(if (isExpanded) R.string.contract else R.string.expand)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (batteryHistory.isEmpty()) {
                EmptyChartPlaceholder()
            } else {
                BatteryHistoryLineChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight)
                        .clip(RoundedCornerShape(12.dp)),
                    batteryHistory = batteryHistory,
                    config = BatteryChartConfig(
                        metrics = selectedMetrics,
                        timeFormat = "HH:mm",
                        showCharging = true,
                        showTemperature = selectedMetrics.contains(BatteryMetric.TEMPERATURE),
                    ),
                    animationDuration = 800,
                    showGrid = true,
                    touchEnabled = true,
                    onDataPointSelected = onDataPointSelected
                )
            }
        }
    }
}

@Composable
private fun EmptyChartPlaceholder() {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(
                colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.round_do_not_disturb_alt_24),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.history_empty),
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.history_empty_description),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BatteryStateDetailsCard(
    batteryState: BatteryState,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val stateIcon = remember(batteryState.isCharging) {
        if (batteryState.isCharging) R.drawable.round_bolt_24 else R.drawable.round_battery_6_bar_24
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.history_point_details_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colorScheme.onSurface
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Información principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailInfoCard(
                    title = stringResource(R.string.history_point_level),
                    value = "${batteryState.batteryLevel}%",
                    icon = ImageVector.vectorResource(id = R.drawable.round_battery_6_bar_24),
                    iconColor = getBatteryLevelColor(batteryState.batteryLevel),
                    modifier = Modifier.weight(1f)
                )

                DetailInfoCard(
                    title = stringResource(R.string.history_point_status),
                    value = stringResource(if (batteryState.isCharging)R.string.status_charging else R.string.status_discharging),
                    icon = ImageVector.vectorResource(id = stateIcon),
                    iconColor = if (batteryState.isCharging) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Información técnica
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailInfoCard(
                    title = stringResource(R.string.history_point_temperature),
                    value = "${batteryState.temperature / 10f}°C",
                    icon = ImageVector.vectorResource(id = R.drawable.round_thermostat_24),
                    iconColor = getTemperatureColor(batteryState.temperature / 10f),
                    modifier = Modifier.weight(1f)
                )

                DetailInfoCard(
                    title = stringResource(R.string.history_point_voltage),
                    value = "${String.format("%.2f", batteryState.voltage / 1000f)}V",
                    icon = ImageVector.vectorResource(id = R.drawable.round_electric_bolt_24),
                    iconColor = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Información adicional
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                DetailRow(
                    label = stringResource(R.string.history_point_charging_type),
                    value = stringResource(batteryState.chargingType.nameId),
                    icon = ImageVector.vectorResource(id = R.drawable.round_power_24)
                )

                batteryState.current?.let { current ->
                    DetailRow(
                        label = stringResource(R.string.history_point_current),
                        value = "${current/1000}mA",
                        icon = ImageVector.vectorResource(id = R.drawable.round_electric_bolt_24)
                    )
                }

                DetailRow(
                    label = stringResource(R.string.history_point_time),
                    value = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                        .format(Date(batteryState.timestamp)),
                    icon = ImageVector.vectorResource(id = R.drawable.round_schedule_24)
                )
            }
        }
    }
}

@Composable
private fun DetailInfoCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = iconColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = colorScheme.onSurface
        )
    }
}

@Composable
private fun StatisticsSummaryCard(batteryHistory: List<BatteryState>) {
    val colorScheme = MaterialTheme.colorScheme
    val locale = androidx.compose.ui.text.intl.Locale.current.platformLocale
    val stats = calculateStatistics(batteryHistory, locale)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.outline_assessment_24),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.history_statistical_summary_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = stringResource(R.string.history_average_level),
                    value = "${stats.averageBatteryLevel}%",
                    icon = ImageVector.vectorResource(id = R.drawable.round_battery_6_bar_24),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.history_average_temperature),
                    value = "${stats.averageTemperature}°C",
                    icon = ImageVector.vectorResource(id = R.drawable.round_thermostat_24),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.history_charging_time),
                    value = "${stats.chargingTimePercentage}%",
                    icon = ImageVector.vectorResource(id = R.drawable.round_bolt_24),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Utility functions
private fun getMetricIcon(metric: BatteryMetric): Int {
    return when (metric) {
        BatteryMetric.BATTERY_LEVEL -> R.drawable.round_battery_6_bar_24
        BatteryMetric.TEMPERATURE -> R.drawable.round_thermostat_24
        BatteryMetric.VOLTAGE -> R.drawable.round_electric_bolt_24
        else -> R.drawable.outline_analytics_24
    }
}

private fun getMetricColor(metric: BatteryMetric): Color {
    return when (metric) {
        BatteryMetric.BATTERY_LEVEL -> Color(0xFF4CAF50)
        BatteryMetric.TEMPERATURE -> Color(0xFFFF9800)
        BatteryMetric.VOLTAGE -> Color(0xFF2196F3)
        else -> Color(0xFF9C27B0)
    }
}

private fun getBatteryLevelColor(level: Int): Color {
    return when {
        level > 60 -> Color(0xFF4CAF50)
        level > 30 -> Color(0xFFFF9800)
        level > 15 -> Color(0xFFFF5722)
        else -> Color(0xFFD32F2F)
    }
}

private fun getTemperatureColor(temp: Float): Color {
    return when {
        temp > 45 -> Color(0xFFFF5722)
        temp > 35 -> Color(0xFFFF9800)
        temp < 10 -> Color(0xFF2196F3)
        else -> Color(0xFF4CAF50)
    }
}

private fun getDateRange(batteryHistory: List<BatteryState>): String? {
    if (batteryHistory.isEmpty()) return null

    val oldest = batteryHistory.minByOrNull { it.timestamp }?.timestamp ?: 0
    val newest = batteryHistory.maxByOrNull { it.timestamp }?.timestamp ?: 0

    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    return "${dateFormat.format(Date(oldest))} - ${dateFormat.format(Date(newest))}"
}

private data class BatteryStatistics(
    val averageBatteryLevel: Int,
    val averageTemperature: String,
    val chargingTimePercentage: Int
)

private fun calculateStatistics(batteryHistory: List<BatteryState>, locale: Locale): BatteryStatistics {
    if (batteryHistory.isEmpty()) {
        return BatteryStatistics(0, "0.0", 0)
    }

    val avgBattery = batteryHistory.map { it.batteryLevel }.average().toInt()
    val avgTemp = batteryHistory.map { it.temperature / 10f }.average()
    val chargingCount = batteryHistory.count { it.isCharging }
    val chargingPercent = (chargingCount * 100) / batteryHistory.size

    return BatteryStatistics(
        averageBatteryLevel = avgBattery,
        averageTemperature = avgTemp.toDecimalFormat(locale, 1),
        chargingTimePercentage = chargingPercent
    )
}
