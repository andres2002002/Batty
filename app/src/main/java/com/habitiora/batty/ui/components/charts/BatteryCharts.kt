package com.habitiora.batty.ui.components.charts

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.habitiora.batty.domain.model.BatteryDataPoint
import com.habitiora.batty.domain.model.ChartType

@Composable
fun BatteryChartByType(
    type: ChartType,
    data: List<BatteryDataPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
) {
    when (type) {
        ChartType.LEVEL       -> LevelChart(data, modifier, height)
        ChartType.TEMPERATURE -> TemperatureChart(data, modifier, height)
        ChartType.CURRENT     -> CurrentChart(data, modifier, height)
    }
}

@Composable
private fun LevelChart(
    data: List<BatteryDataPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
) {
    val primary = MaterialTheme.colorScheme.primary
    TimeSeriesChart(
        points      = data.map { TimeSeriesPoint(it.timestamp, it.level.toFloat()) },
        modifier    = modifier,
        chartHeight = height,
        lineColor   = primary,
        fillColor   = primary.copy(alpha = 0.10f),
        yAxisConfig = YAxisConfig(
            fixedMin    = 0f,
            fixedMax    = 100f,
            gridLines   = listOf(0f, 25f, 50f, 75f, 100f),
            labelFormat = { "${it.toInt()}%" }
        ),
    )
}

@Composable
private fun TemperatureChart(
    data: List<BatteryDataPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
) {
    val tertiary = MaterialTheme.colorScheme.tertiary
    val temps    = data.map { it.temperature }
    val minTemp  = (temps.minOrNull() ?: 0f) - 2f
    val maxTemp  = (temps.maxOrNull() ?: 60f) + 2f

    TimeSeriesChart(
        points      = data.map { TimeSeriesPoint(it.timestamp, it.temperature) },
        modifier    = modifier,
        chartHeight = height,
        lineColor   = tertiary,
        fillColor   = tertiary.copy(alpha = 0.10f),
        yAxisConfig = YAxisConfig(
            fixedMin    = minTemp,
            fixedMax    = maxTemp,
            labelCount  = 5,
            labelFormat = { "${"%.1f".format(it)}°" }
        ),
    )
}

@Composable
private fun CurrentChart(
    data: List<BatteryDataPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
) {
    val secondary = MaterialTheme.colorScheme.secondary
    val currents  = data.map { it.currentMa }
    val maxCurrent = (currents.maxOrNull() ?: 1000f) * 1.1f

    TimeSeriesChart(
        points      = data.map { TimeSeriesPoint(it.timestamp, it.currentMa) },
        modifier    = modifier,
        chartHeight = height,
        lineColor   = secondary,
        fillColor   = secondary.copy(alpha = 0.10f),
        yAxisConfig = YAxisConfig(
            fixedMin    = 0f,
            fixedMax    = maxCurrent,
            labelCount  = 5,
            labelFormat = { "${"%.0f".format(it)}mA" }
        ),
    )
}