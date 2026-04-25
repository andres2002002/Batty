package com.habitiora.batty.ui.components.chip

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Chip binario on/off para SystemStateCard.
 * Activo → Tonal (primaryContainer). Inactivo → Surface.
 */
@Composable
fun SystemStateChip(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    BattyChip(
        label = label,
        modifier = modifier,
        style = if (active) BattyChipStyle.Tonal else BattyChipStyle.Surface,
        colors = if (active) BattyChipDefaults.primary() else BattyChipDefaults.surface(),
        leadingIcon = leadingIcon,
    )
}