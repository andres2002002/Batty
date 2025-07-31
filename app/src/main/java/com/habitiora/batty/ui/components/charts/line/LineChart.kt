package com.habitiora.batty.ui.components.charts.line

import android.content.Context
import android.graphics.Color
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.habitiora.batty.R
import com.habitiora.batty.domain.model.AnimationDirection
import com.habitiora.batty.domain.model.BatteryState
import com.habitiora.batty.utils.toDecimalFormat
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

// Enum para diferentes métricas de batería
enum class BatteryMetric(@StringRes val labelId: Int, val unit: String, val color: Int) {
    BATTERY_LEVEL(R.string.metrics_battery_level, "%", Color.GREEN),
    TEMPERATURE(R.string.metrics_temperature, "°C", Color.RED),
    VOLTAGE(R.string.metrics_voltage, "V", Color.BLUE),
    CURRENT(R.string.metrics_current, "mA", Color.YELLOW),
    CHARGING_RATE(R.string.metrics_charging_rate, "%/h", Color.CYAN),
    DISCHARGE_RATE(R.string.metrics_discharge_rate, "%/h", Color.MAGENTA)
}

// Configuración para el chart de batería
data class BatteryChartConfig(
    val metrics: List<BatteryMetric> = listOf(BatteryMetric.BATTERY_LEVEL),
    val timeFormat: String = "HH:mm",
    val showCharging: Boolean = true,
    val showTemperature: Boolean = false,
    val maxDataPoints: Int = 300
)

@Composable
fun BatteryHistoryLineChart(
    modifier: Modifier = Modifier,
    batteryHistory: List<BatteryState>,
    config: BatteryChartConfig = BatteryChartConfig(),
    animationDuration: Int = 1500,
    animationDirection: AnimationDirection = AnimationDirection.VERTICAL,
    showGrid: Boolean = true,
    touchEnabled: Boolean = true,
    axisTextColor: Int = Color.WHITE,
    axisTextSize: Float = 12f,
    lineWidth: Float = 2.5f,
    circleRadius: Float = 1f,
    curveMode: LineDataSet.Mode = LineDataSet.Mode.CUBIC_BEZIER,
    onDataPointSelected: (BatteryState?) -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val locale = androidx.compose.ui.text.intl.Locale.current.platformLocale

    // Procesar y filtrar datos de batería
    val processedData = remember(batteryHistory, config) {
        processBatteryData(batteryHistory, config)
    }

    // Crear entradas del chart para cada métrica
    val chartEntries = remember(processedData, config) {
        createBatteryChartEntries(processedData, config)
    }

    // Formatear etiquetas de tiempo
    val timeLabels = remember(processedData) {
        createTimeLabels(processedData, config.timeFormat)
    }

    // Crear datasets para cada métrica
    val dataSets = remember(chartEntries, config, lineWidth, circleRadius, curveMode) {
        createBatteryDataSets(
            context = context,
            chartEntries = chartEntries,
            config = config,
            lineWidth = lineWidth,
            circleRadius = circleRadius,
            curveMode = curveMode
        )
    }

    // Formateador del eje X (tiempo)
    val xAxisFormatter = remember(timeLabels) {
        createTimeAxisFormatter(timeLabels)
    }

    // Formateador de valores Y
    val yAxisFormatter = remember(config) {
        createValueAxisFormatter(config, locale)
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            Timber.d("los datos en factory son: ${processedData.size}, ${dataSets.size}, ${timeLabels.size}, ${config.metrics.size}, ${config.timeFormat}")
            LineChart(context).apply {
                setupBatteryChartConfiguration(
                    showGrid = showGrid,
                    touchEnabled = touchEnabled,
                    axisTextColor = axisTextColor,
                    axisTextSize = axisTextSize,
                    textColor = colorScheme.onSurface
                )
            }
        },
        update = { lineChart ->
            val dataMsg = StringBuilder()
            dataMsg.append("batteryHistory: ${batteryHistory.size}, ")
            dataMsg.append("processedData: ${processedData.size}, ")
            dataMsg.append("dataSets: ${dataSets.size}, ")
            dataMsg.append("timeLabels: ${timeLabels.size}, ")
            dataMsg.append("metrics: ${config.metrics.size}, ")
            dataMsg.append("timeFormat: ${config.timeFormat}")
            Timber.d(dataMsg.toString())

            // Verificar que hay datos
            if (dataSets.isEmpty()) {
                Timber.w("No datasets available")
                return@AndroidView
            }

            // Actualizar datos solo si es necesario
            if (lineChart.data?.dataSetCount != dataSets.size ||
                hasBatteryDataChanged(lineChart, dataSets)) {
                lineChart.data = LineData(dataSets)
                Timber.d("Chart data updated")
            }

            lineChart.apply {
                val totalPoints = processedData.size.coerceAtLeast(1)
                Timber.d("Total points: $totalPoints")

                // Configurar eje X
                with(xAxis) {
                    valueFormatter = xAxisFormatter
                    extraBottomOffset = 10f
                    axisMaximum = (totalPoints - 1).toFloat()
                    axisMinimum = 0f
                    Timber.d("X axis configured: min=0, max=${totalPoints-1}")
                }

                // Configurar eje Y con rangos apropiados
                with(axisLeft) {
                    valueFormatter = yAxisFormatter

                    // Calcular rango Y basado en los datos
                    val allValues = dataSets.flatMap { dataset ->
                        (0 until dataset.entryCount).map { i -> dataset.getEntryForIndex(i).y }
                    }

                    if (allValues.isNotEmpty()) {
                        val minY = 0f
                        val maxY = (allValues.maxOrNull() ?: 100f).coerceAtLeast(100f)
                        val padding = (maxY - minY) * 0.1f

                        axisMinimum = (minY - padding).coerceAtLeast(0f)
                        axisMaximum = maxY + padding

                        Timber.d("Y axis configured: min=${axisMinimum}, max=${axisMaximum}")
                    }
                }

                // Configurar rango visible
                val minVisibleRange = (totalPoints * 0.2f).coerceIn(3f..10f)
                val visibleRange = (totalPoints * 1f).coerceIn(10f..150f)

                setVisibleXRangeMinimum(minVisibleRange)
                setVisibleXRangeMaximum(visibleRange)

                // Mover al final (datos más recientes) solo si hay muchos puntos
                if (totalPoints > visibleRange) {
                    moveViewToX((totalPoints - 1).toFloat())
                }

                performBatteryAnimation(animationDuration, animationDirection)
                invalidate()

                Timber.d("Chart updated and invalidated")
            }

            // Configurar listener para selección de puntos
            lineChart.setOnChartValueSelectedListener(
                createBatteryValueSelectedListener(processedData, onDataPointSelected)
            )
        }
    )
}

// Función para procesar datos de batería
private fun processBatteryData(
    batteryHistory: List<BatteryState>,
    config: BatteryChartConfig
): List<BatteryState> {
    val processed = batteryHistory
        .sortedBy { it.timestamp }
        .takeLast(config.maxDataPoints)
        .filter { it.batteryLevel >= 0 } // Filtrar datos inválidos

    Timber.d("Processed battery data: ${processed.size} items")

    return processed
}

// Crear entradas del chart para cada métrica
private fun createBatteryChartEntries(
    data: List<BatteryState>,
    config: BatteryChartConfig
): Map<BatteryMetric, List<Entry>> {
    val entries = config.metrics.associateWith { metric ->
        data.mapIndexedNotNull { index, batteryState ->
            val value = when (metric) {
                BatteryMetric.BATTERY_LEVEL -> batteryState.batteryLevel.toFloat()
                BatteryMetric.TEMPERATURE -> {
                    if (batteryState.temperature > 0) batteryState.temperature / 10f else null
                }
                BatteryMetric.VOLTAGE -> {
                    if (batteryState.voltage > 0) batteryState.voltage / 1000f else null
                }
                BatteryMetric.CURRENT -> {
                    batteryState.current?.let { abs(it).toFloat() }
                }
                BatteryMetric.CHARGING_RATE -> batteryState.chargingRate
                BatteryMetric.DISCHARGE_RATE -> batteryState.dischargeRate
            }

            value?.let {
                Entry(index.toFloat(), it)
            }
        }
    }

    return entries
}

// Crear etiquetas de tiempo
private fun createTimeLabels(
    data: List<BatteryState>,
    timeFormat: String
): List<String> {
    val dateFormat = SimpleDateFormat(timeFormat, Locale.getDefault())
    return data.map { batteryState ->
        dateFormat.format(Date(batteryState.timestamp))
    }
}

// Crear datasets para las métricas de batería
private fun createBatteryDataSets(
    context: Context,
    chartEntries: Map<BatteryMetric, List<Entry>>,
    config: BatteryChartConfig,
    lineWidth: Float,
    circleRadius: Float,
    curveMode: LineDataSet.Mode
): List<ILineDataSet> {
    val datasets = chartEntries.mapNotNull { (metric, entries) ->
        if (entries.isEmpty()) {
            Timber.w("No entries for metric: ${metric.name}")
            return@mapNotNull null
        }

        Timber.d("Creating dataset for ${metric.name} with ${entries.size} entries")

        LineDataSet(entries, "${context.getString(metric.labelId)} (${metric.unit})").apply {
            color = metric.color
            valueTextSize = 0f // Ocultar valores en puntos
            setDrawValues(false) // No mostrar valores en cada punto
            this.lineWidth = lineWidth
            valueTextColor = Color.WHITE
            setCircleColor(metric.color)
            this.circleRadius = circleRadius
            circleHoleColor = Color.TRANSPARENT
            circleHoleRadius = circleRadius * 0.5f
            mode = curveMode

            // Configurar transparencia del área bajo la curva para nivel de batería
            if (metric == BatteryMetric.BATTERY_LEVEL) {
                setDrawFilled(true)
                fillColor = metric.color
                fillAlpha = 30
            }

            // Hacer líneas más visibles
            setDrawCircles(false)
            setDrawCircleHole(false)

            Timber.d("Dataset created for ${metric.name}: $entryCount entries")
        }
    }

    Timber.d("Total datasets created: ${datasets.size}")
    return datasets
}

// Crear formateador para el eje X (tiempo)
private fun createTimeAxisFormatter(timeLabels: List<String>): ValueFormatter {
    return object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            return when {
                index < 0 || index >= timeLabels.size -> ""
                index == 0 -> timeLabels.first()
                index == timeLabels.lastIndex -> timeLabels.last()
                index % (timeLabels.size / 5).coerceAtLeast(1) == 0 -> timeLabels[index]
                else -> ""
            }
        }
    }
}

// Crear formateador para el eje Y (valores)
private fun createValueAxisFormatter(config: BatteryChartConfig, locale: Locale): ValueFormatter {
    return object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return when {
                config.metrics.contains(BatteryMetric.BATTERY_LEVEL) -> "${value.toInt()}%"
                config.metrics.contains(BatteryMetric.TEMPERATURE) -> "${value.toInt()}°C"
                config.metrics.contains(BatteryMetric.VOLTAGE) -> "${value.toDecimalFormat(locale, 1)}V"
                else -> value.toInt().toString()
            }
        }
    }
}

// Crear listener para selección de valores
private fun createBatteryValueSelectedListener(
    data: List<BatteryState>,
    onDataPointSelected: (BatteryState?) -> Unit
): OnChartValueSelectedListener {
    return object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry?, h: Highlight?) {
            try {
                val index = h?.x?.toInt()
                if (index != null && index in data.indices) {
                    onDataPointSelected(data[index])
                } else {
                    onDataPointSelected(null)
                }
            } catch (e: Exception) {
                Timber.e(e, "onValueSelected: ${e.message}")
                onDataPointSelected(null)
            }
        }

        override fun onNothingSelected() {
            onDataPointSelected(null)
        }
    }
}

// Configuración específica para chart de batería
private fun LineChart.setupBatteryChartConfiguration(
    showGrid: Boolean,
    touchEnabled: Boolean,
    axisTextColor: Int,
    axisTextSize: Float,
    textColor: androidx.compose.ui.graphics.Color
) {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    with(description) { isEnabled = false }

    // Configuración de interacción
    setTouchEnabled(touchEnabled)
    setPinchZoom(true)
    setDrawGridBackground(false)
    setDrawBorders(false)
    setScaleEnabled(true)
    isDragEnabled = true

    // Configurar eje X (tiempo)
    xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        granularity = 1f
        setDrawGridLines(showGrid)
        this.textColor = axisTextColor
        textSize = axisTextSize
        setCenterAxisLabels(false)
        setLabelCount(5, false)
    }

    // Configurar eje Y izquierdo (valores principales)
    axisLeft.apply {
        setDrawGridLines(showGrid)
        this.textColor = axisTextColor
        textSize = axisTextSize
        granularity = 1f
        setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
    }

    // Ocultar eje Y derecho
    axisRight.isEnabled = false

    // Configurar leyenda
    legend.apply {
        isEnabled = true
        this.textColor = textColor.hashCode()
        textSize = axisTextSize
        verticalAlignment = Legend.LegendVerticalAlignment.TOP
        horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        orientation = Legend.LegendOrientation.VERTICAL
        setDrawInside(true)
    }

    // Configurar comportamiento de gestos
    onChartGestureListener = object : OnChartGestureListener {
        override fun onChartGestureStart(
            me: MotionEvent?,
            lastPerformedGesture: ChartTouchListener.ChartGesture?
        ) {
            parent.requestDisallowInterceptTouchEvent(true)
        }

        override fun onChartGestureEnd(
            me: MotionEvent?,
            lastPerformedGesture: ChartTouchListener.ChartGesture?
        ) {
            parent.requestDisallowInterceptTouchEvent(false)
        }

        override fun onChartLongPressed(me: MotionEvent?) {}
        override fun onChartDoubleTapped(me: MotionEvent?) {}
        override fun onChartSingleTapped(me: MotionEvent?) {}
        override fun onChartFling(
            me1: MotionEvent?,
            me2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ) {}
        override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}
        override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {}
    }
}

// Verificar cambios en datos de batería
private fun hasBatteryDataChanged(chart: LineChart, newDataSets: List<ILineDataSet>): Boolean {
    val currentData = chart.data?.dataSets ?: return true
    if (currentData.size != newDataSets.size) return true

    return currentData.indices.any { i ->
        val current = currentData[i]
        val new = newDataSets[i]
        current.entryCount != new.entryCount || current.label != new.label
    }
}

// Animación específica para datos de batería
private fun LineChart.performBatteryAnimation(
    duration: Int,
    direction: AnimationDirection
) {
    if (duration > 0) {
        val easing = Easing.EaseInOutCubic
        when (direction) {
            AnimationDirection.VERTICAL -> animateY(duration, easing)
            AnimationDirection.HORIZONTAL -> animateX(duration, easing)
            AnimationDirection.BOTH -> animateXY(duration, duration, easing, easing)
        }
    }
}