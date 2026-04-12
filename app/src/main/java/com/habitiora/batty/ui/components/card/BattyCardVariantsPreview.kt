package com.habitiora.batty.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.habitiora.batty.ui.theme.BattyTheme

@PreviewLightDark
@Preview(name = "BattyCard variants", showBackground = true)
@Composable
private fun BattyCardVariantsPreview() {
    BattyTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Default
                BattyCard(variant = BattyCardVariant.Default) {
                    Text(
                        text = "Default",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        text = "DetailsCard / ElectricalCard",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                // Elevated con header
                BattyCard(
                    variant = BattyCardVariant.Elevated,
                    header = {
                        Text(
                            text = "Battery Level",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                ) {
                    Text(
                        text = "Elevated — LevelCard",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                // Outlined
                BattyCard(variant = BattyCardVariant.Outlined) {
                    Text(
                        text = "Outlined",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        text = "Estados de alerta / borde funcional",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                // Filled
                BattyCard(
                    variant = BattyCardVariant.Filled,
                    header = {
                        Icon(
                            imageVector = Icons.Outlined.BatteryAlert,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                ) {
                    Text(
                        text = "Filled",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "MonitorToggleCard / SystemStateCard",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                // Clickable
                BattyCard(
                    variant = BattyCardVariant.Default,
                    onClick = {},
                ) {
                    Text(
                        text = "Clickable — ripple activo",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}