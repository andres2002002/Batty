package com.habitiora.batty.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessLow
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.EnergySavingsLeaf
import androidx.compose.material.icons.outlined.NightShelter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.habitiora.batty.R
import com.habitiora.batty.ui.components.SectionHeader
import com.habitiora.batty.ui.components.card.BattyCard
import com.habitiora.batty.ui.components.card.BattyCardVariant
import com.habitiora.batty.ui.components.chip.SystemStateChip

/**
 * Card de estado del sistema — pantalla, battery saver y doze.
 *
 * Los chips se renderizan siempre — muestran estado activo/inactivo
 * via [SystemStateChip] en lugar de ocultarse.
 *
 * @param isScreenOn Pantalla encendida.
 * @param isBatterySaver Modo ahorro de batería activo.
 * @param isDozeMode Modo doze activo.
 * @param modifier Modifier externo.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SystemStateCard(
    isScreenOn: Boolean,
    isBatterySaver: Boolean,
    isDozeMode: Boolean,
    modifier: Modifier = Modifier,
) {
    BattyCard(
        variant = BattyCardVariant.Default,
        modifier = modifier,
        header = { SectionHeader(title = stringResource(R.string.dashboard_system_title)) },
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SystemStateChip(
                label = if (isScreenOn) {
                    stringResource(R.string.dashboard_screen_on)
                } else {
                    stringResource(R.string.dashboard_screen_off)
                },
                active = isScreenOn,
                leadingIcon = if (isScreenOn) {
                    Icons.Outlined.BrightnessMedium
                } else {
                    Icons.Outlined.BrightnessLow
                },
            )
            SystemStateChip(
                label = stringResource(R.string.dashboard_battery_saver),
                active = isBatterySaver,
                leadingIcon = Icons.Outlined.EnergySavingsLeaf,
            )
            SystemStateChip(
                label = stringResource(R.string.dashboard_doze),
                active = isDozeMode,
                leadingIcon = Icons.Outlined.NightShelter,
            )
        }
    }
}
