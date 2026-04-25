package com.habitiora.batty.ui.screens.dashboard.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.habitiora.batty.ui.components.InfoRow
import com.habitiora.batty.ui.components.InfoRowEmphasis
import com.habitiora.batty.ui.components.SectionHeader
import com.habitiora.batty.ui.components.card.BattyCard
import com.habitiora.batty.ui.components.card.BattyCardVariant
import com.habitiora.batty.ui.utils.BatteryFormatter

/**
 * Card de datos eléctricos de la batería.
 *
 * Todos los valores numéricos usan -1 / -1f como centinela "no disponible"
 * — la conversión a null para InfoRow ocurre en [BatteryFormatter].
 *
 * @param currentNowMa Corriente instantánea en mA. -1f si no disponible.
 * @param currentAvgMa Corriente promedio en mA. -1f si no disponible.
 * @param watts Potencia en W. -1f / 0f si no disponible.
 * @param chargeCounterMah Carga restante en mAh. -1 si no disponible.
 * @param estimatedMinutesRemaining Tiempo estimado en minutos. -1 si no disponible.
 * @param isCharging Controla el label del tiempo estimado.
 * @param modifier Modifier externo.
 */
@Composable
fun ElectricalCard(
    voltageMv: Int,
    currentNowMa: Float,
    currentAvgMa: Float,
    watts: Float,
    chargeCounterMah: Int,
    fullCapacityMah: Int,
    estimatedMinutesRemaining: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier,
) {
    BattyCard(
        variant = BattyCardVariant.Default,
        modifier = modifier,
        header = { SectionHeader(title = "Electrical") },
    ) {
        InfoRow(
            label = "Voltage",
            value = BatteryFormatter.voltage(voltageMv),
            emphasis = InfoRowEmphasis.ValueHighlighted,
        )
        ElectricalCardDivider()
        InfoRow(
            label = "Current (now)",
            value = BatteryFormatter.current(currentNowMa),
            emphasis = InfoRowEmphasis.ValueHighlighted,
        )
        ElectricalCardDivider()
        if (currentAvgMa > 0f) {
            InfoRow(
                label = "Current (avg)",
                value = BatteryFormatter.current(currentAvgMa),
                emphasis = InfoRowEmphasis.ValueHighlighted,
            )
            ElectricalCardDivider()
        }
        InfoRow(
            label = "Power",
            value = BatteryFormatter.watts(watts),
            emphasis = InfoRowEmphasis.ValueHighlighted,
        )
        ElectricalCardDivider()
        InfoRow(
            label = "Charge remaining",
            value = BatteryFormatter.chargeCounter(chargeCounterMah),
            emphasis = InfoRowEmphasis.Default,
        )
        ElectricalCardDivider()
        InfoRow(
            label = "Current capacity",
            value = BatteryFormatter.chargeCounter(fullCapacityMah),
            emphasis = InfoRowEmphasis.Default,
        )
        ElectricalCardDivider()
        InfoRow(
            label = if (isCharging) "Full in" else "Empty in",
            value = BatteryFormatter.estimatedTime(estimatedMinutesRemaining),
            emphasis = if (estimatedMinutesRemaining > 0) {
                InfoRowEmphasis.ValueHighlighted
            } else {
                InfoRowEmphasis.Muted
            },
        )
    }
}

@Composable
private fun ElectricalCardDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}