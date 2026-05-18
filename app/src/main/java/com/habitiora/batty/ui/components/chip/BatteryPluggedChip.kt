package com.habitiora.batty.ui.components.chip

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.Usb
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.habitiora.batty.domain.model.BatteryPlugged

@Composable
fun BatteryPluggedChip(
    plugged: BatteryPlugged,
    modifier: Modifier = Modifier,
) {
    if (plugged == BatteryPlugged.NONE) return

    val icon = when (plugged) {
        BatteryPlugged.AC -> Icons.Outlined.Power
        BatteryPlugged.USB -> Icons.Outlined.Usb
        BatteryPlugged.WIRELESS -> Icons.Outlined.Wifi
        BatteryPlugged.NONE -> null
    }

    BattyChip(
        label = stringResource(plugged.labelRes),
        modifier = modifier,
        style = BattyChipStyle.Tonal,
        colors = BattyChipDefaults.tertiary(),
        leadingIcon = icon,
    )
}