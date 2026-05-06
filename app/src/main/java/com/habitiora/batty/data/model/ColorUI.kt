package com.habitiora.batty.data.model

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.runtime.Immutable

@Immutable
data class ColorUI(
    val name: String,
    val light: Color,
    val dark: Color,
) {
    val color: Color
        @Composable
        get() = if (isSystemInDarkTheme()) dark else light

    companion object {
        // Light ajustado a GM3 Base (Tonos 50/60) para mayor viveza
        val Red = ColorUI(
            name = "Red",
            light = Color(0xFFE33629), // Más brillante y puro
            dark = Color(0xFFFFB4AB)
        )
        val Blue = ColorUI(
            name = "Blue",
            light = Color(0xFF1A73E8), // Azul vibrante estándar de Google
            dark = Color(0xFF9ECAFF)
        )
        val Green = ColorUI(
            name = "Green",
            light = Color(0xFF1E8E3E), // Verde más luminoso
            dark = Color(0xFF8ED996)
        )
        val Yellow = ColorUI(
            name = "Yellow",
            light = Color(0xFFE3A008), // Dorado vivo, visible en fondos claros
            dark = Color(0xFFF6BE3F)
        )
        val Orange = ColorUI(
            name = "Orange",
            light = Color(0xFFE37400),
            dark = Color(0xFFFFB87A)
        )
        val Purple = ColorUI(
            name = "Purple",
            light = Color(0xFF9334E6),
            dark = Color(0xFFD0BCFF)
        )
        val Pink = ColorUI(
            name = "Pink",
            light = Color(0xFFD81B60),
            dark = Color(0xFFFFB0C8)
        )
        val Cyan = ColorUI(
            name = "Cyan",
            light = Color(0xFF12B5CB),
            dark = Color(0xFF4FD8EB)
        )

        val allColors = listOf(Red, Blue, Green, Yellow, Orange, Purple, Pink, Cyan)
    }
}

@Stable
fun getInterpolatedColor(progress: Float, colors: List<Color>): Color {
    if (colors.isEmpty()) return Color.Transparent
    if (colors.size == 1) return colors.first()

    val coercedProgress = progress.coerceIn(0f, 1f)
    if (coercedProgress <= 0f) return colors.first()
    if (coercedProgress >= 1f) return colors.last()

    val scaledProgress = coercedProgress * (colors.size - 1)
    val index = scaledProgress.toInt()
    val fraction = scaledProgress - index

    return lerp(colors[index], colors[index + 1], fraction)
}