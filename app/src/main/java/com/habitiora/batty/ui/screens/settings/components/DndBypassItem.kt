package com.habitiora.batty.ui.screens.settings.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.habitiora.batty.domain.model.DndBypassState
import com.habitiora.batty.ui.components.chip.BattyChip
import com.habitiora.batty.ui.components.chip.BattyChipDefaults
import com.habitiora.batty.ui.components.chip.BattyChipStyle
import com.habitiora.batty.ui.components.settings.SettingsItemDefaults

/**
 * Item read-only que refleja el estado DND del canal crítico.
 *
 * No tiene switch — el usuario activa "Override Do Not Disturb"
 * manualmente en la configuración del canal vía [Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS].
 *
 * El badge se refresca en ON_RESUME via DisposableEffect en el caller.
 *
 * @param dndBypassState Estado actual del canal. Fuente de verdad: [com.habitiora.batty.services.NotificationHelper.getDndBypassState].
 * @param channelId ID del canal crítico para abrir su configuración directa.
 */
@Composable
fun DndBypassItem(
    dndBypassState: DndBypassState,
    channelId: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = SettingsItemDefaults.HorizontalPadding,
                vertical = SettingsItemDefaults.VerticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(SettingsItemDefaults.IconSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.NotificationsActive,
            contentDescription = null,
            modifier = Modifier.size(SettingsItemDefaults.IconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(SettingsItemDefaults.DescriptionSpacing),
        ) {
            Text(
                text = "Override Do Not Disturb",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Activate in channel settings to allow critical alerts during DND",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            DndStatusBadge(active = dndBypassState.channelCanBypass)

            IconButton(
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = "Open channel settings",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun DndStatusBadge(active: Boolean) {
    BattyChip(
        label = if (active) "Override enabled" else "Override disabled",
        style = if (active) BattyChipStyle.Tonal else BattyChipStyle.Surface,
        colors = if (active) BattyChipDefaults.primary() else BattyChipDefaults.surface(),
    )
}