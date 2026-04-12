package com.habitiora.batty.ui.utils

import kotlin.math.abs

/**
 * Conversores de valores de dominio a strings de display.
 * Maneja la convención -1 = "no disponible" → null para InfoRow.
 */
object BatteryFormatter {

    fun temperature(celsius: Float): String? =
        if (celsius <= 0f) null else "%.1f °C".format(celsius)

    fun voltage(millivolts: Int): String? =
        if (millivolts <= 0) null else "%,d mV".format(millivolts).replace(",", " ")

    fun current(milliamps: Float): String? =
        if (milliamps < 0f) null else "%,d mA".format(abs(milliamps).toInt()).replace(",", " ")

    fun watts(w: Float): String? =
        if (w <= 0f) null else "%.2f W".format(w)

    fun chargeCounter(mah: Int): String? =
        if (mah <= 0) null else "%,d mAh".format(mah).replace(",", " ")

    fun estimatedTime(minutes: Int): String? {
        if (minutes <= 0) return null
        val hours = minutes / 60
        val remaining = minutes % 60
        return when {
            hours > 0 && remaining > 0 -> "${hours}h ${remaining}m"
            hours > 0 -> "${hours}h"
            else -> "${remaining}m"
        }
    }

    fun technology(tech: String?): String? =
        tech?.takeIf { it.isNotBlank() }
}