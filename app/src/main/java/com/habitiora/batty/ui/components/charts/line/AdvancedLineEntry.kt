package com.habitiora.batty.ui.components.charts.line

import com.github.mikephil.charting.data.Entry

/**
 * Clase de entrada de datos avanzada para el gráfico de líneas.
 *
 * @param label Etiqueta asociada a la entrada de datos.
 * @param code Código asociado a la entrada de datos.
 * @param value Valor numérico asociado a la entrada de datos.
 */
class AdvancedLineEntry(
    val group: Int,
    val label: String,
    val code: Int,
    val value: Double,
): Entry(
    code.toFloat(),
    value.toFloat()
)