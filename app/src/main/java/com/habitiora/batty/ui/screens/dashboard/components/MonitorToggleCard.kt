package com.habitiora.batty.ui.screens.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HeartBroken
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.habitiora.batty.R
import com.habitiora.batty.domain.model.ServiceErrorCause
import com.habitiora.batty.domain.model.ServiceState
import com.habitiora.batty.ui.components.card.BattyCard
import com.habitiora.batty.ui.components.card.BattyCardVariant

/**
 * Card de toggle del servicio de monitoreo. Vive en el Dashboard.
 *
 * Usa [BattyCardVariant.Filled] para diferenciarse visualmente del resto
 * de cards informativos — es el único control de acción del Dashboard.
 *
 * @param serviceState Estado actual del servicio.
 * @param onToggle Callback al cambiar el switch.
 * @param modifier Modifier externo.
 */
@Composable
fun MonitorToggleCard(
    serviceState: ServiceState,
    onToggle: (Boolean) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BattyCard(
        variant = BattyCardVariant.Filled,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ServiceStateIcon(state = serviceState)
                Spacer(modifier = Modifier.width(0.dp))
                MonitorToggleLabels(state = serviceState)
            }

            MonitorStateControl(
                serviceState = serviceState,
                onToggle = onToggle,
                onRetry = onRetry
            )
        }
    }
}

@Composable
private fun ServiceStateIcon(state: ServiceState, modifier: Modifier = Modifier) {
    val (icon, tint) = when (state) {
        ServiceState.Active -> Icons.Outlined.MonitorHeart to MaterialTheme.colorScheme.primary
        ServiceState.Inactive -> Icons.Outlined.HeartBroken to MaterialTheme.colorScheme.onSurfaceVariant
        ServiceState.Loading -> Icons.Outlined.Sync to MaterialTheme.colorScheme.secondary
        is ServiceState.Error -> Icons.Outlined.ErrorOutline to MaterialTheme.colorScheme.error
    }

    // Rotación continua en estado Loading
    val rotation by if (state == ServiceState.Loading) {
        val transition = rememberInfiniteTransition(label = "loading_rotation")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing)
            ),
            label = "rotation"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = modifier.graphicsLayer { rotationZ = rotation }
    )
}

@Composable
private fun MonitorToggleLabels(
    state: ServiceState
) {
    val (text, color) = when (state) {
        ServiceState.Active ->
            stringResource(R.string.dashboard_monitor_active_label) to MaterialTheme.colorScheme.onSurfaceVariant

        ServiceState.Inactive ->
            stringResource(R.string.dashboard_monitor_inactive_label) to MaterialTheme.colorScheme.onSurfaceVariant

        ServiceState.Loading ->
            stringResource(R.string.dashboard_monitor_starting_label) to MaterialTheme.colorScheme.secondary

        is ServiceState.Error -> {
            val msg = when (state.cause) {
                ServiceErrorCause.UNEXPECTED_DISCONNECT -> stringResource(R.string.dashboard_monitor_error_disconnect)
                ServiceErrorCause.BIND_FAILED -> stringResource(R.string.dashboard_monitor_error_bind)
            }
            msg to MaterialTheme.colorScheme.error
        }
    }
    val titleColor by animateColorAsState(
        targetValue = if (state == ServiceState.Active) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 300),
        label = "monitor_toggle_title_color",
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = stringResource(R.string.dashboard_monitor_title),
            style = MaterialTheme.typography.bodyLarge,
            color = titleColor,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
        )
    }
}


@Composable
private fun MonitorStateControl(
    serviceState: ServiceState,
    onToggle: (Boolean) -> Unit,
    onRetry: () -> Unit
) {
    when (serviceState) {
        ServiceState.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        is ServiceState.Error -> {
            // Error: switch desactivado + botón retry
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onRetry, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = stringResource(R.string.dashboard_retry),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
                MonitorSwitch(
                    checked = false,
                    onCheckedChange = onToggle
                )
            }
        }

        ServiceState.Active, ServiceState.Inactive -> {
            MonitorSwitch(
                checked = serviceState == ServiceState.Active,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun MonitorSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
){
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        thumbContent = {
            if (checked) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.batty_icon_open),
                    contentDescription = stringResource(R.string.dashboard_monitor_active_label),
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            } else {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.batty_icon_close),
                    contentDescription = stringResource(R.string.dashboard_monitor_inactive_label),
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        }
    )
}