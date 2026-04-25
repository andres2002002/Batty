package com.habitiora.batty.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.habitiora.batty.domain.model.BatteryHealth
import com.habitiora.batty.ui.theme.BattyTheme

@PreviewLightDark
@Composable
private fun DetailsCardPreview() {
    BattyTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Estado normal
                DetailsCard(
                    temperatureCelsius = 29.5f,
                    health = BatteryHealth.GOOD,
                    technology = "Li-ion",
                )

                // Temperatura alta (tertiary)
                DetailsCard(
                    temperatureCelsius = 41.0f,
                    health = BatteryHealth.OVERHEAT,
                    technology = "Li-ion",
                )

                // Temperatura crítica (error)
                DetailsCard(
                    temperatureCelsius = 47.5f,
                    health = BatteryHealth.OVERHEAT,
                    technology = "Li-poly",
                )

                // Datos no disponibles
                DetailsCard(
                    temperatureCelsius = -1f,
                    health = BatteryHealth.UNKNOWN,
                    technology = null,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ElectricalCardPreview() {
    BattyTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Cargando — datos completos
                ElectricalCard(
                    voltageMv = 4120,
                    currentNowMa = 1450f,
                    currentAvgMa = 1380f,
                    watts = 5.85f,
                    chargeCounterMah = 2100,
                    fullCapacityMah = 3000,
                    estimatedMinutesRemaining = 55,
                    isCharging = true,
                )

                // Descargando — datos completos
                ElectricalCard(
                    voltageMv = 4120,
                    currentNowMa = 780f,
                    currentAvgMa = 820f,
                    watts = 3.12f,
                    chargeCounterMah = 1450,
                    fullCapacityMah = 2500,
                    estimatedMinutesRemaining = 112,
                    isCharging = false,
                )

                // Hardware sin soporte de corriente
                ElectricalCard(
                    voltageMv = 4120,
                    currentNowMa = -1f,
                    currentAvgMa = -1f,
                    watts = -1f,
                    chargeCounterMah = -1,
                    fullCapacityMah = -1,
                    estimatedMinutesRemaining = -1,
                    isCharging = false,
                )
            }
        }
    }
}