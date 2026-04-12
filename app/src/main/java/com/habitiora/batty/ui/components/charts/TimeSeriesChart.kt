package com.habitiora.batty.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TimeSeriesPoint(val timestamp: Long, val value: Float)

/**
 * Configuración del eje Y.
 * [fixedMin]/[fixedMax]: rango fijo (ej: 0-100 para level).
 * null = auto-range basado en los datos con padding.
 */
data class YAxisConfig(
    val fixedMin: Float? = null,
    val fixedMax: Float? = null,
    val gridLines: List<Float>? = null,   // valores en los que dibujar grid lines
    val labelCount: Int = 5,
    val labelFormat: (Float) -> String = { "%.0f".format(it) },
)

@Composable
fun TimeSeriesChart(
    points: List<TimeSeriesPoint>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 200.dp,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = lineColor.copy(alpha = 0.10f),
    gridColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
    yAxisConfig: YAxisConfig = YAxisConfig(),
    showDots: Boolean = points.size <= 48,
    labelStyle: TextStyle = TextStyle(fontSize = 10.sp),
) {
    if (points.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val yMin =
        yAxisConfig.fixedMin ?: (points.minOf { it.value } - points.maxOf { it.value } * 0.05f)
    val yMax =
        yAxisConfig.fixedMax ?: (points.maxOf { it.value } + points.maxOf { it.value } * 0.05f)
    val yRange = (yMax - yMin).coerceAtLeast(1f)

    val xMin = points.minOf { it.timestamp }
    val xMax = points.maxOf { it.timestamp }
    val xRange = (xMax - xMin).coerceAtLeast(1L)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
    ) {
        val labelAreaW = 36.dp.toPx()
        val padTop = 8.dp.toPx()
        val padBottom = 20.dp.toPx()
        val chartLeft = labelAreaW
        val chartW = size.width - chartLeft - 4.dp.toPx()
        val chartH = size.height - padTop - padBottom

        fun valueToY(v: Float) = padTop + chartH * (1f - (v - yMin) / yRange)
        fun timestampToX(t: Long) = chartLeft + chartW * ((t - xMin).toFloat() / xRange)

        // ── Grid lines ─────────────────────────────────────────────────
        val gridValues = yAxisConfig.gridLines
            ?: buildList {
                val step = yRange / (yAxisConfig.labelCount - 1)
                repeat(yAxisConfig.labelCount) { i -> add(yMin + step * i) }
            }

        gridValues.forEach { v ->
            val y = valueToY(v)
            drawLine(
                color = gridColor,
                start = Offset(chartLeft, y),
                end = Offset(chartLeft + chartW, y),
                strokeWidth = 1.dp.toPx()
            )
            // Labels Y
            val label = yAxisConfig.labelFormat(v)
            val measured = textMeasurer.measure(label, labelStyle.copy(color = labelColor))
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(0f, y - measured.size.height / 2f)
            )
        }

        val offsets = points.map { p -> Offset(timestampToX(p.timestamp), valueToY(p.value)) }

        // ── Fill ───────────────────────────────────────────────────────
        val fillPath = Path().apply {
            moveTo(offsets.first().x, size.height - padBottom)
            lineTo(offsets.first().x, offsets.first().y)
            offsets.drop(1).forEach { lineTo(it.x, it.y) }
            lineTo(offsets.last().x, size.height - padBottom)
            close()
        }
        drawPath(fillPath, color = fillColor)

        // ── Line ───────────────────────────────────────────────────────
        val linePath = Path().apply {
            moveTo(offsets.first().x, offsets.first().y)
            offsets.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(linePath, color = lineColor, style = Stroke(width = 2.dp.toPx()))

        // ── Dots ───────────────────────────────────────────────────────
        if (showDots) {
            offsets.forEach {
                drawCircle(color = lineColor, radius = 3.dp.toPx(), center = it)
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 1.5f.dp.toPx(),
                    center = it
                )
            }
        }

        // ── X axis baseline ────────────────────────────────────────────
        drawLine(
            color = gridColor,
            start = Offset(chartLeft, size.height - padBottom),
            end = Offset(chartLeft + chartW, size.height - padBottom),
            strokeWidth = 1.dp.toPx()
        )
    }
}