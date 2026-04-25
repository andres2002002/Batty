package com.habitiora.batty.ui.components.battery

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.habitiora.batty.data.model.ColorUI
import com.habitiora.batty.data.model.getInterpolatedColor

/**
 * Fuente de verdad única para el color funcional del nivel de batería.
 * Mapeado estrictamente a la paleta semántica de Material 3.
 */
@Composable
fun batteryLevelColor(level: Int): Color = getInterpolatedColor(
    level / 100f, listOf(
        ColorUI.Red.color, ColorUI.Yellow.color, ColorUI.Green.color
    )
)

/**
 * Variante de track (fondo) usando los contenedores nativos de M3.
 * Esto garantiza contraste perfecto en Light/Dark mode sin manipular opacidades.
 */
@Composable
fun batteryLevelTrackColor(level: Int): Color = when {
    level >= 60 -> MaterialTheme.colorScheme.primaryContainer
    level >= 25 -> MaterialTheme.colorScheme.secondaryContainer
    else -> MaterialTheme.colorScheme.errorContainer
}

// Nuevos tokens para el fondo y texto del Hero Card
@Composable
fun batteryLevelContainerColor(level: Int): Color = when {
    level >= 60 -> MaterialTheme.colorScheme.primaryContainer
    level >= 25 -> MaterialTheme.colorScheme.tertiaryContainer
    else -> MaterialTheme.colorScheme.errorContainer
}

@Composable
fun batteryLevelOnContainerColor(level: Int): Color = when {
    level >= 60 -> MaterialTheme.colorScheme.onPrimaryContainer
    level >= 25 -> MaterialTheme.colorScheme.onTertiaryContainer
    else -> MaterialTheme.colorScheme.onErrorContainer
}