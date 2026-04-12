package com.habitiora.batty.ui.components.chip

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.habitiora.batty.domain.model.BatteryHealth

@Composable
fun BatteryHealthChip(
    health: BatteryHealth,
    modifier: Modifier = Modifier,
) {
    val (style, colors) = when (health) {
        BatteryHealth.GOOD -> BattyChipStyle.Tonal to BattyChipDefaults.primary()
        BatteryHealth.OVERHEAT,
        BatteryHealth.OVER_VOLTAGE -> BattyChipStyle.Tonal to BattyChipDefaults.error()
        BatteryHealth.DEAD -> BattyChipStyle.Tonal to BattyChipDefaults.error()
        BatteryHealth.COLD -> BattyChipStyle.Outlined to BattyChipDefaults.outlined()
        BatteryHealth.UNKNOWN,
        BatteryHealth.UNSPECIFIED_FAILURE -> BattyChipStyle.Outlined to BattyChipDefaults.outlined()
    }

    BattyChip(
        label = stringResource(health.nameId),
        modifier = modifier,
        style = style,
        colors = colors,
    )
}