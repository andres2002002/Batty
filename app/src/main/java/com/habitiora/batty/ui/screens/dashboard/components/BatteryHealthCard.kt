package com.habitiora.batty.ui.screens.dashboard.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.habitiora.batty.data.model.ColorUI
import com.habitiora.batty.domain.model.BatteryHealth
import com.habitiora.batty.ui.components.InfoColumn
import com.habitiora.batty.ui.components.InfoColumnDefaults
import com.habitiora.batty.ui.components.InfoRowEmphasis
import com.habitiora.batty.ui.components.chip.BatteryHealthChip

@Composable
fun BatteryHealthCard(health: BatteryHealth, modifier: Modifier = Modifier) {
    InfoColumn(
        modifier = modifier,
        label = "Battery Health",
        value = stringResource(health.nameId),
        icon = {
            Icon(
                modifier = Modifier.size(InfoColumnDefaults.IconSize.Large),
                imageVector = Icons.Outlined.HealthAndSafety,
                tint = ColorUI.Green.color,
                contentDescription = null
            )
        },
        emphasis = InfoRowEmphasis.ValueHighlighted,
        valueColor = healthColor(health),
    )

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
