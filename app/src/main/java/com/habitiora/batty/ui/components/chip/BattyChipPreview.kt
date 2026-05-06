package com.habitiora.batty.ui.components.chip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.BrightnessLow
import androidx.compose.material.icons.outlined.NightShelter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.habitiora.batty.domain.model.BatteryHealth
import com.habitiora.batty.domain.model.BatteryPlugged
import com.habitiora.batty.domain.model.BatteryStatus
import com.habitiora.batty.ui.theme.BattyTheme

@OptIn(ExperimentalLayoutApi::class)
@PreviewLightDark
@Composable
private fun BattyChipPreview() {
    BattyTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("BatteryStatus", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BatteryStatus.entries.forEach { BatteryStatusChip(it) }
                }

                Text("BatteryPlugged", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BatteryPlugged.entries
                        .filter { it != BatteryPlugged.NONE }
                        .forEach { BatteryPluggedChip(it) }
                }

                Text("BatteryHealth", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BatteryHealth.entries.forEach { BatteryHealthChip(it) }
                }

                Text("SystemState — active / inactive", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SystemStateChip(
                        label = "Screen on",
                        active = true,
                        leadingIcon = Icons.Outlined.BrightnessMedium,
                    )
                    SystemStateChip(
                        label = "Screen off",
                        active = false,
                        leadingIcon = Icons.Outlined.BrightnessLow,
                    )
                    SystemStateChip(label = "Battery saver", active = true)
                    SystemStateChip(label = "Battery saver", active = false)
                    SystemStateChip(
                        label = "Doze",
                        active = true,
                        leadingIcon = Icons.Outlined.NightShelter,
                    )
                }

                Text("Primitivo base — estilos", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BattyChip(label = "Tonal", style = BattyChipStyle.Tonal)
                    BattyChip(label = "Outlined", style = BattyChipStyle.Outlined)
                    BattyChip(label = "Surface", style = BattyChipStyle.Surface)
                    BattyChip(
                        label = "Error",
                        style = BattyChipStyle.Tonal,
                        colors = BattyChipDefaults.error(),
                    )
                    BattyChip(
                        label = "Tertiary",
                        style = BattyChipStyle.Tonal,
                        colors = BattyChipDefaults.tertiary(),
                    )
                }
            }
        }
    }
}