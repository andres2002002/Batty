package com.habitiora.batty.utils

import java.util.Locale

fun Float.toDecimalFormat(locale: Locale, decimalPlaces: Int= 2): String =
    String.format(locale, "%.${decimalPlaces}f", this)

fun Double.toDecimalFormat(locale: Locale, decimalPlaces: Int= 2): String =
    String.format(locale, "%.${decimalPlaces}f", this)