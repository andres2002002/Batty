package com.habitiora.batty.ui.components.charts

import android.text.format.DateFormat
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.habitiora.batty.domain.model.BatteryDataPoint
import com.habitiora.batty.domain.model.ChartType
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import androidx.compose.ui.graphics.Brush
import java.util.Date

@Composable
fun BatteryChartByType(
    type: ChartType,
    data: List<BatteryDataPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 250.dp,
) {
    if (data.isEmpty()) return

    val baseTime = remember(data) { data.first().timestamp }
    val dataMap = remember(data) {
        data.associateBy { ((it.timestamp - baseTime) / 1000).toDouble() }
    }

    val hoursVisible = 26.0
    val viewportSeconds = remember(hoursVisible) { Zoom.x(hoursVisible * 3600.0) }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data, type) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = data.map { ((it.timestamp - baseTime) / 1000).toDouble() },
                    y = data.map {
                        when (type) {
                            ChartType.LEVEL -> it.level.toDouble()
                            ChartType.TEMPERATURE -> it.temperature.toDouble()
                            ChartType.CURRENT -> it.currentMa.toDouble()
                        }
                    }
                )
            }
        }
    }

    val lineColor = when (type) {
        ChartType.LEVEL -> MaterialTheme.colorScheme.primary
        ChartType.TEMPERATURE -> MaterialTheme.colorScheme.tertiary
        ChartType.CURRENT -> MaterialTheme.colorScheme.secondary
    }

    val marker = rememberBatteryMarker(dataMap, baseTime)

    val bottomAxisFormatter = remember(baseTime) {
        CartesianValueFormatter { _, x, _ ->
            val date = Date(baseTime + (x * 1000).toLong())
            DateFormat.format("HH:mm", date).toString()
        }
    }

    val component = rememberShapeComponent(
        shape = CircleShape,
        fill = Fill(lineColor),
        strokeFill = Fill(MaterialTheme.colorScheme.surface),
        strokeThickness = 2.dp
    )

    // Solo dibuja los puntos si hay pocos datos. Evita el amontonamiento con alta densidad.
    val pointProvider = remember(dataMap, component) {
        if (data.size <= 48) {
            LineCartesianLayer.PointProvider.single(
                LineCartesianLayer.Point(component = component)
            )
        } else null
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(Fill(lineColor)),
                        areaFill = LineCartesianLayer.AreaFill.single(Fill(lineColor.copy(alpha = 0.15f))),
                        pointProvider = pointProvider,
                    )
                ),
            ),
            startAxis = VerticalAxis.rememberStart(
                itemPlacer = VerticalAxis.ItemPlacer.step({
                    when(type) {
                        ChartType.LEVEL -> 25.0
                        ChartType.TEMPERATURE -> 5.0
                        ChartType.CURRENT -> 500.0
                    }
                })
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = bottomAxisFormatter,
                guideline = null
            ),
            marker = marker
        ),
        modelProducer = modelProducer,
        modifier = modifier.fillMaxWidth().height(height),
        zoomState = rememberVicoZoomState(zoomEnabled = true, initialZoom = viewportSeconds),
    )
}

@Composable
fun rememberBatteryMarker(
    dataMap: Map<Double, BatteryDataPoint>,
    baseTime: Long
): CartesianMarker {
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
    val indicatorColor = MaterialTheme.colorScheme.primary

    val labelBackground = rememberShapeComponent(
        fill = Fill(surfaceColor),
        shape = RoundedCornerShape(8.dp)
    )

    val labelText = rememberTextComponent(
        lineCount = 5,
        background = labelBackground,
        padding = Insets(12.dp, 8.dp),
    )

    val indicator = rememberShapeComponent(
        fill = Fill(indicatorColor),
        shape = CircleShape
    )

    val guideline = rememberLineComponent(
        fill = Fill(onSurfaceColor.copy(alpha = 0.2f)),
        thickness = 2.dp
    )

    return rememberDefaultCartesianMarker(
        label = labelText,
        indicator = { indicator },
        guideline = guideline,
        valueFormatter = remember(dataMap, baseTime) {
            DefaultCartesianMarker.ValueFormatter { _, targets ->
                val xValue = targets.first().x
                val point = dataMap[xValue]
                if (point != null) {
                    val timeString = DateFormat.format("HH:mm:ss", Date(point.timestamp))
                    val status = if (point.isCharging) "🔌 Conectado" else "🔋 Batería"

                    """
                    ⏳ $timeString
                    $status
                    Nivel: ${point.level}%
                    Temp: ${point.temperature}ºC
                    Corr: ${point.currentMa.toInt()}mA
                    """.trimIndent()
                } else {
                    "Sin datos"
                }
            }
        }
    )
}