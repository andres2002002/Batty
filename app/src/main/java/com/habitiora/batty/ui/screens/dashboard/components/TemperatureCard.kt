package com.habitiora.batty.ui.screens.dashboard.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habitiora.batty.data.model.ColorUI
import com.habitiora.batty.ui.components.InfoColumn
import com.habitiora.batty.ui.components.InfoColumnDefaults
import com.habitiora.batty.ui.components.InfoRowEmphasis
import com.habitiora.batty.ui.utils.BatteryFormatter

@Composable
fun TemperatureCard(
    modifier: Modifier = Modifier,
    temperatureCelsius: Float,
) {
    InfoColumn(
        modifier = modifier,
        label = "Temperature",
        value = BatteryFormatter.temperature(temperatureCelsius),
        icon = {
            Icon(
                modifier = Modifier.size(InfoColumnDefaults.IconSize.Large),
                imageVector = Icons.Outlined.Thermostat,
                tint = if (temperatureCelsius > 40) ColorUI.Red.color else ColorUI.Green.color,
                contentDescription = null
            )
        },
        emphasis = InfoRowEmphasis.ValueHighlighted,
        valueColor = temperatureColor(temperatureCelsius),
    )
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
