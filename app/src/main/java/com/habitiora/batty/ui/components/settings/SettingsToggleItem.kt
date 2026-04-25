package com.habitiora.batty.ui.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Row de settings con Switch.
 *
 * @param title Texto principal.
 * @param checked Estado actual del switch.
 * @param onCheckedChange Callback de cambio. Si null, el item es read-only.
 * @param modifier Modifier externo.
 * @param description Texto secundario opcional bajo [title].
 * @param icon Ícono leading opcional.
 * @param enabled Cuando false aplica alpha [SettingsItemDefaults.DisabledAlpha] y
 *                desactiva el Switch. No oculta el item.
 */
@Composable
fun SettingsToggleItem(
    title: String,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else SettingsItemDefaults.DisabledAlpha)
            .padding(
                horizontal = SettingsItemDefaults.HorizontalPadding,
                vertical = SettingsItemDefaults.VerticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(SettingsItemDefaults.IconSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(SettingsItemDefaults.IconSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(SettingsItemDefaults.DescriptionSpacing),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled && onCheckedChange != null,
        )
    }
}