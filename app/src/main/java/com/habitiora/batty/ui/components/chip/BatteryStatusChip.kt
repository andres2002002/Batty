package com.habitiora.batty.ui.components.chip

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.BatteryUnknown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.habitiora.batty.domain.model.BatteryStatus

@Composable
fun BatteryStatusChip(
    status: BatteryStatus,
    modifier: Modifier = Modifier,
) {
    val (colors, icon, style) = when (status) {
        BatteryStatus.CHARGING -> Triple(BattyChipDefaults.primary(), Icons.Outlined.BatteryChargingFull, BattyChipStyle.Tonal)
        BatteryStatus.FULL -> Triple(BattyChipDefaults.primary(), Icons.Outlined.BatteryFull, BattyChipStyle.Tonal)
        BatteryStatus.DISCHARGING,
        BatteryStatus.NOT_CHARGING -> Triple(BattyChipDefaults.surface(), null, BattyChipStyle.Surface)
        BatteryStatus.UNKNOWN -> Triple(BattyChipDefaults.outlined(), Icons.Outlined.BatteryUnknown, BattyChipStyle.Outlined)
    }

    BattyChip(
        label = status.label,
        modifier = modifier,
        style = style,
        colors = colors,
        leadingIcon = icon,
    )
}