package com.habitiora.batty.ui.components.chip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Stable
enum class BattyChipStyle {
    Tonal,
    Outlined,
    Surface,
}

@Immutable
data class BattyChipColors(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color = Color.Transparent,
)

object BattyChipDefaults {
    @Composable
    fun tonal(
        containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    ) = BattyChipColors(containerColor = containerColor, contentColor = contentColor)

    @Composable
    fun outlined(
        borderColor: Color = MaterialTheme.colorScheme.outline,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
    ) = BattyChipColors(
        containerColor = Color.Transparent,
        contentColor = contentColor,
        borderColor = borderColor,
    )

    @Composable
    fun surface(
        containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    ) = BattyChipColors(containerColor = containerColor, contentColor = contentColor)

    @Composable
    fun primary() = tonal(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )

    @Composable
    fun secondary() = tonal(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )

    @Composable
    fun tertiary() = tonal(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    )

    @Composable
    fun error() = tonal(
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    )
}

/**
 * Chip informativo base redimensionado a los estándares de Material 3.
 */
@Composable
fun BattyChip(
    label: String,
    modifier: Modifier = Modifier,
    style: BattyChipStyle = BattyChipStyle.Surface,
    colors: BattyChipColors? = null,
    leadingIcon: ImageVector? = null,
) {
    val resolvedColors = colors ?: when (style) {
        BattyChipStyle.Tonal -> BattyChipDefaults.tonal()
        BattyChipStyle.Outlined -> BattyChipDefaults.outlined()
        BattyChipStyle.Surface -> BattyChipDefaults.surface()
    }

    val border = if (style == BattyChipStyle.Outlined || resolvedColors.borderColor != Color.Transparent) {
        BorderStroke(width = 1.dp, color = resolvedColors.borderColor)
    } else null

    Surface(
        // Altura estándar de M3 para Chips no interactivos
        modifier = modifier.height(32.dp),
        // Pill-shape da un look mucho más moderno y nativo
        shape = MaterialTheme.shapes.large,
        color = resolvedColors.containerColor,
        contentColor = resolvedColors.contentColor,
        border = border,
    ) {
        Row(
            // Padding horizontal más generoso para que "respire"
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    // Subimos el tamaño del icono a un estándar legible
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = label,
                // Subimos de labelSmall a labelMedium para mejor lectura
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}