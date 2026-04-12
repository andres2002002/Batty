package com.habitiora.batty.ui.components.battery

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
enum class BatteryLevelBarSize(val height: Dp) {
    Small(4.dp),   // Uso compacto (notificación persistente, stats)
    Medium(8.dp),  // Uso estándar (LevelCard)
    Large(12.dp)   // Uso prominente (pantalla dedicada)
}

object BatteryLevelBarDefaults {
    val ProgressAnimationSpec = tween<Float>(
        durationMillis = 600,
        easing = FastOutSlowInEasing,
    )
    val ColorAnimationSpec = tween<Color>(
        durationMillis = 400,
        easing = FastOutSlowInEasing,
    )
}

/**
 * Barra de nivel de batería con color reactivo y animaciones suaves.
 *
 * @param level Nivel actual 0–100.
 * @param modifier Modifier externo. [fillMaxWidth] aplicado por defecto internamente.
 * @param size Token de tamaño. Controla la altura de la barra.
 * @param animated Si true aplica animaciones de progreso y color. Desactivar en
 * previews estáticos o listas con muchos items.
 */
@Composable
fun BatteryLevelBar(
    level: Int,
    modifier: Modifier = Modifier,
    size: BatteryLevelBarSize = BatteryLevelBarSize.Medium,
    animated: Boolean = true,
) {
    val clampedLevel = level.coerceIn(0, 100)
    val targetProgress = clampedLevel / 100f

    val targetIndicatorColor = batteryLevelColor(clampedLevel)
    val targetTrackColor = batteryLevelTrackColor(clampedLevel)

    val progressSpec = if (animated) BatteryLevelBarDefaults.ProgressAnimationSpec else snap()
    val colorSpec = if (animated) BatteryLevelBarDefaults.ColorAnimationSpec else snap()

    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = progressSpec,
        label = "battery_level_progress",
    )

    val indicatorColor by animateColorAsState(
        targetValue = targetIndicatorColor,
        animationSpec = colorSpec,
        label = "battery_level_indicator_color",
    )

    val trackColor by animateColorAsState(
        targetValue = targetTrackColor,
        animationSpec = colorSpec,
        label = "battery_level_track_color",
    )

    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .height(size.height),
        color = indicatorColor,
        trackColor = trackColor,
        strokeCap = StrokeCap.Round,
    )
}
