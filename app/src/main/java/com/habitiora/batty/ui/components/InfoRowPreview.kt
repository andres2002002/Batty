package com.habitiora.batty.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.habitiora.batty.ui.components.card.BattyCard
import com.habitiora.batty.ui.components.card.BattyCardVariant
import com.habitiora.batty.ui.theme.BattyTheme

@PreviewLightDark
@Composable
private fun InfoRowPreview() {
    BattyTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                // Contexto real: DetailsCard
                BattyCard(
                    variant = BattyCardVariant.Default,
                    header = { SectionHeader(title = "Details") },
                ) {
                    InfoRow(
                        label = "Temperature",
                        value = "32.5 °C",
                        emphasis = InfoRowEmphasis.Default,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    InfoRow(
                        label = "Voltage",
                        value = "4 120 mV",
                        emphasis = InfoRowEmphasis.Default,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    InfoRow(
                        label = "Technology",
                        value = "Li-ion",
                        emphasis = InfoRowEmphasis.Default,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    InfoRow(
                        label = "Health",
                        value = "Good",
                        emphasis = InfoRowEmphasis.Default,
                        valueColor = MaterialTheme.colorScheme.primary,
                    )
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))

                // Contexto real: ElectricalCard
                BattyCard(
                    variant = BattyCardVariant.Default,
                    header = { SectionHeader(title = "Electrical") },
                ) {
                    InfoRow(
                        label = "Current (now)",
                        value = "1 250 mA",
                        emphasis = InfoRowEmphasis.ValueHighlighted,
                    )
                    InfoRow(
                        label = "Current (avg)",
                        value = "1 180 mA",
                        emphasis = InfoRowEmphasis.ValueHighlighted,
                    )
                    InfoRow(
                        label = "Power",
                        value = "5.1 W",
                        emphasis = InfoRowEmphasis.ValueHighlighted,
                    )
                    InfoRow(
                        label = "Cycle count",
                        value = null,
                        emphasis = InfoRowEmphasis.Muted,
                    )
                }
            }
        }
    }
}