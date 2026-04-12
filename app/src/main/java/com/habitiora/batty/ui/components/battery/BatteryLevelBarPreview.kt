package com.habitiora.batty.ui.components.battery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.habitiora.batty.ui.components.card.BattyCard
import com.habitiora.batty.ui.components.card.BattyCardVariant
import com.habitiora.batty.ui.theme.BattyTheme

private val previewLevels = listOf(
    100 to "Full (100%)",
    85 to "High (85%)",
    60 to "Primary threshold (60%)",
    59 to "Tertiary threshold (59%)",
    45 to "Mid (45%)",
    25 to "Tertiary threshold (25%)",
    24 to "Error threshold (24%)",
    10 to "Critical (10%)",
    4 to "Very critical (4%)",
    0 to "Empty (0%)",
)

@PreviewLightDark
@Composable
private fun BatteryLevelBarPreview() {
    BattyTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Todos los niveles — Medium
                BattyCard(
                    variant = BattyCardVariant.Default,
                    header = {
                        com.habitiora.batty.ui.components.SectionHeader(title = "Medium — niveles")
                    },
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        previewLevels.forEach { (level, label) ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            BatteryLevelBar(
                                level = level,
                                size = BatteryLevelBarSize.Medium,
                                animated = false,
                            )
                        }
                    }
                }

                // Comparativa de tamaños con nivel fijo
                BattyCard(
                    variant = BattyCardVariant.Default,
                    header = {
                        com.habitiora.batty.ui.components.SectionHeader(title = "Tamaños — nivel 45%")
                    },
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BatteryLevelBarSize.entries.forEach { barSize ->
                            Text(
                                text = barSize.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            BatteryLevelBar(
                                level = 45,
                                size = barSize,
                                animated = false,
                            )
                        }
                    }
                }
            }
        }
    }
}