package com.habitiora.batty.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.habitiora.batty.domain.model.ServiceState
import com.habitiora.batty.ui.theme.BattyTheme

@PreviewLightDark
@Composable
private fun MonitorToggleCardPreview() {
    BattyTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                var active by remember { mutableStateOf(true) }
                MonitorToggleCard(
                    serviceState = ServiceState.Active,
                    onToggle = { active = it },
                    onRetry = {},
                )
                MonitorToggleCard(
                    serviceState = ServiceState.Inactive,
                    onToggle = { active = it },
                    onRetry = {},
                )
                MonitorToggleCard(
                    serviceState = ServiceState.Loading,
                    onToggle = { active = it },
                    onRetry = {},
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SystemStateCardPreview() {
    BattyTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Pantalla encendida, sin ahorro
                SystemStateCard(
                    isScreenOn = true,
                    isBatterySaver = false,
                    isDozeMode = false,
                )
                // Pantalla apagada, battery saver y doze activos
                SystemStateCard(
                    isScreenOn = false,
                    isBatterySaver = true,
                    isDozeMode = true,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HealthCardPreview() {
    BattyTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Salud buena — dato completo
                HealthCard(
                    capacityHealthPercent = 91,
                    cycleCount = 142,
                    fullCapacityMah = 4512,
                    designCapacityMah = 4950,
                )
                // Salud media
                HealthCard(
                    capacityHealthPercent = 67,
                    cycleCount = 380,
                    fullCapacityMah = 3316,
                    designCapacityMah = 4950,
                )
                // Salud baja — error color
                HealthCard(
                    capacityHealthPercent = 44,
                    cycleCount = 620,
                    fullCapacityMah = 2178,
                    designCapacityMah = 4950,
                )
                // Solo cycle count disponible — sin barra
                HealthCard(
                    capacityHealthPercent = -1,
                    cycleCount = 245,
                    fullCapacityMah = -1,
                    designCapacityMah = -1,
                )
            }
        }
    }
}