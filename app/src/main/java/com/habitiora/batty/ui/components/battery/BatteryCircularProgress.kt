package com.habitiora.batty.ui.components.battery

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Indicador circular de nivel de batería.
 * Reemplaza al antiguo BatteryLevelBar lineal, centralizando la animación y el texto del porcentaje.
 *
 * @param level Nivel actual 0–100.
 * @param modifier Modifier externo.
 * @param size Diámetro total del componente.
 * @param strokeWidth Grosor de la línea del progreso.
 */
@Composable
fun BatteryCircularProgress(
    level: Int,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 14.dp,
) {
    val clampedLevel = level.coerceIn(0, 100)

    val levelColor by animateColorAsState(
        targetValue = batteryLevelColor(clampedLevel),
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "circular_progress_color",
    )

    val trackColor = batteryLevelTrackColor(clampedLevel)

    val progress by animateFloatAsState(
        targetValue = clampedLevel / 100f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "circular_progress_anim"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(size),
            color = levelColor,
            trackColor = trackColor,
            strokeWidth = strokeWidth,
            strokeCap = StrokeCap.Round,
        )

        Row(verticalAlignment = Alignment.Bottom) {
            AnimatedContent(
                targetState = clampedLevel,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(150))
                },
                label = "level_number",
            ) { animatedLevel ->
                Text(
                    text = animatedLevel.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = levelColor,
                )
            }

            Text(
                text = "%",
                style = MaterialTheme.typography.headlineMedium,
                color = levelColor,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
            )
        }
    }
}