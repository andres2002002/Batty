package com.habitiora.batty.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.habitiora.batty.R
import com.habitiora.batty.domain.model.BatteryPlugged
import com.habitiora.batty.domain.model.BatteryStatus
import com.habitiora.batty.ui.components.battery.BatteryCircularProgress
import com.habitiora.batty.ui.components.card.BattyCard
import com.habitiora.batty.ui.components.card.BattyCardVariant
import com.habitiora.batty.ui.components.card.BattyCardDefaults
import com.habitiora.batty.ui.components.chip.BatteryPluggedChip
import com.habitiora.batty.ui.components.chip.BatteryStatusChip
import com.habitiora.batty.ui.utils.BatteryFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LevelCard(
    level: Int,
    status: BatteryStatus,
    plugged: BatteryPlugged,
    isCharging: Boolean,
    estimatedMinutesRemaining: Int,
    modifier: Modifier = Modifier,
) {
    BattyCard(
        variant = BattyCardVariant.Elevated,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BattyCardDefaults.InnerSpacing)
        ) {

            // Componente central extraído
            BatteryCircularProgress(
                level = level,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            if (estimatedMinutesRemaining > 0) {
                EstimatedTimeLabelCentral(
                    minutes = estimatedMinutesRemaining,
                    isCharging = isCharging,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BatteryStatusChip(status = status)
                if (plugged != BatteryPlugged.NONE) {
                    BatteryPluggedChip(plugged = plugged)
                }
            }
        }
    }
}

@Composable
private fun EstimatedTimeLabelCentral(
    minutes: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier,
) {
    val prefix = if (isCharging) {
        stringResource(R.string.dashboard_full_in)
    } else {
        stringResource(R.string.dashboard_empty_in)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = prefix,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = BatteryFormatter.estimatedTime(minutes) ?: "",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}