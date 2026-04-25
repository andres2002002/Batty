package com.habitiora.batty.ui.components.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.habitiora.batty.ui.components.SectionHeader
import com.habitiora.batty.ui.components.card.BattyCard
import com.habitiora.batty.ui.components.card.BattyCardDefaults
import com.habitiora.batty.ui.components.card.BattyCardVariant
import com.habitiora.batty.ui.theme.BattyTheme

private enum class SaveInterval(val label: String, val description: String? = null) {
    ONE_MIN(label = "1 minute", description = "High battery drain impact"),
    FIVE_MIN(label = "5 minutes", description = "Recommended"),
    FIFTEEN_MIN(label = "15 minutes"),
    THIRTY_MIN(label = "30 minutes"),
}

@PreviewLightDark
@Composable
private fun SettingsItemsPreview() {
    BattyTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                SectionHeader(title = "Monitor")

                BattyCard(
                    variant = BattyCardVariant.Default,
                    contentPadding = BattyCardDefaults.ContentPadding.copy(
                        top = 0.dp,
                        bottom = 0.dp,
                    ),
                ) {
                    var monitorEnabled by remember { mutableStateOf(true) }
                    var notifyEnabled by remember { mutableStateOf(true) }
                    var startOnBoot by remember { mutableStateOf(false) }
                    var onlyCritical by remember { mutableStateOf(false) }
                    var saveInterval by remember { mutableStateOf(SaveInterval.FIVE_MIN) }

                    SettingsToggleItem(
                        title = "Monitor battery",
                        description = "Runs a foreground service to track battery state",
                        checked = monitorEnabled,
                        onCheckedChange = { monitorEnabled = it },
                        icon = Icons.Outlined.PowerSettingsNew,
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    SettingsToggleItem(
                        title = "Battery alerts",
                        description = "Notify when thresholds are crossed",
                        checked = notifyEnabled,
                        onCheckedChange = { notifyEnabled = it },
                        icon = Icons.Outlined.Notifications,
                        enabled = monitorEnabled,
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    SettingsToggleItem(
                        title = "Critical alerts only",
                        description = "Suppress low and high battery notifications",
                        checked = onlyCritical,
                        onCheckedChange = { onlyCritical = it },
                        icon = Icons.Outlined.BatteryAlert,
                        enabled = monitorEnabled && notifyEnabled,
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    SettingsRadioButtonMenu(
                        title = "Snapshot interval",
                        selectedValue = saveInterval,
                        options = SaveInterval.entries.map { interval ->
                            RadioButtonOption(
                                value = interval,
                                label = interval.label,
                                description = interval.description,
                            )
                        },
                        onValueChange = { saveInterval = it },
                        icon = Icons.Outlined.Schedule,
                        enabled = monitorEnabled,
                    )
                }
            }
        }
    }
}

// Extension para poder usar el copy en PaddingValues
private fun androidx.compose.foundation.layout.PaddingValues.copy(
    top: androidx.compose.ui.unit.Dp = this.calculateTopPadding(),
    bottom: androidx.compose.ui.unit.Dp = this.calculateBottomPadding(),
): androidx.compose.foundation.layout.PaddingValues =
    androidx.compose.foundation.layout.PaddingValues(
        start = this.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
        top = top,
        end = this.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
        bottom = bottom,
    )