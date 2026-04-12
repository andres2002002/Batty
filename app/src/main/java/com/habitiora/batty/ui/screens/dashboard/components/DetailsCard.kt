package com.habitiora.batty.ui.screens.dashboard.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.habitiora.batty.domain.model.BatteryHealth
import com.habitiora.batty.ui.components.InfoRow
import com.habitiora.batty.ui.components.InfoRowEmphasis
import com.habitiora.batty.ui.components.SectionHeader
import com.habitiora.batty.ui.components.card.BattyCard
import com.habitiora.batty.ui.components.card.BattyCardVariant
import com.habitiora.batty.ui.utils.BatteryFormatter

/**
 * Card de detalles físicos de la batería.
 *
 * @param temperatureCelsius Temperatura en °C. <= 0 muestra "—".
 * @param voltageMv Voltaje en mV. <= 0 muestra "—".
 * @param health Estado físico de la batería.
 * @param technology Tecnología de celda. Null o blank muestra "—".
 * @param modifier Modifier externo.
 */
@Composable
fun DetailsCard(
    temperatureCelsius: Float,
    voltageMv: Int,
    health: BatteryHealth,
    technology: String?,
    modifier: Modifier = Modifier,
) {
    BattyCard(
        variant = BattyCardVariant.Default,
        modifier = modifier,
        header = { SectionHeader(title = "Details") },
    ) {
        InfoRow(
            label = "Temperature",
            value = BatteryFormatter.temperature(temperatureCelsius),
            emphasis = InfoRowEmphasis.ValueHighlighted,
            valueColor = temperatureColor(temperatureCelsius),
        )
        DetailsCardDivider()
        InfoRow(
            label = "Voltage",
            value = BatteryFormatter.voltage(voltageMv),
            emphasis = InfoRowEmphasis.ValueHighlighted,
        )
        DetailsCardDivider()
        InfoRow(
            label = "Health",
            value = stringResource(health.nameId),
            emphasis = InfoRowEmphasis.ValueHighlighted,
            valueColor = healthColor(health),
        )
        DetailsCardDivider()
        InfoRow(
            label = "Technology",
            value = BatteryFormatter.technology(technology),
            emphasis = InfoRowEmphasis.Default,
        )
    }
}

/**
 * Color funcional para temperatura.
 * < 40°C  → onSurface (normal)
 * 40–45°C → tertiary (warm)
 * > 45°C  → error (hot)
 */
@Composable
private fun temperatureColor(celsius: Float) = when {
    celsius <= 0f -> MaterialTheme.colorScheme.onSurfaceVariant
    celsius >= 45f -> MaterialTheme.colorScheme.error
    celsius >= 40f -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.onSurface
}

/**
 * Color funcional para health — consistente con [BatteryHealthChip].
 */
@Composable
private fun healthColor(health: BatteryHealth) = when (health) {
    BatteryHealth.GOOD -> MaterialTheme.colorScheme.primary
    BatteryHealth.OVERHEAT,
    BatteryHealth.OVER_VOLTAGE,
    BatteryHealth.DEAD -> MaterialTheme.colorScheme.error
    BatteryHealth.COLD -> MaterialTheme.colorScheme.tertiary
    BatteryHealth.UNKNOWN,
    BatteryHealth.UNSPECIFIED_FAILURE -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun DetailsCardDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}