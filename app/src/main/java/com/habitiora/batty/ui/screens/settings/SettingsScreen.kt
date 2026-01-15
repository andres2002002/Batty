package com.habitiora.batty.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitiora.batty.R
import com.habitiora.batty.services.PermissionRequest
import com.habitiora.batty.services.PermissionsHelper
import com.habitiora.batty.ui.components.RequestPermission
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val showBatteryAlerts by viewModel.isNotificationEnabled.collectAsState()
    val isDNDEnabled by viewModel.isDNDEnabled.collectAsState()
    val isMonitoringEnabled by viewModel.isMonitoringEnabled.collectAsState()
    val lowThreshold by viewModel.lowThreshold.collectAsState()
    val highThreshold by viewModel.highThreshold.collectAsState()

    val (lowSteps, lowRange) = 49 to 1f..49f
    val (highSteps, highRange) = 50 to 51f..100f

    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    if (showBatteryAlerts) {
        RequestPermission(
            request = PermissionRequest.NotificationAccess,
            onGranted = {
                viewModel.toggleNotification(true)
                        },
            onDenied = {
                viewModel.toggleNotification(false)
                viewModel.toggleDND(false)
            }
        )
    }
    if (isDNDEnabled && showBatteryAlerts) {
        RequestPermission(
            request = PermissionRequest.DndAccess,
            onGranted = {
                viewModel.toggleDND(true)
                        },
            onDenied = {
                viewModel.toggleDND(false)
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.toggleDND(!PermissionsHelper.needs(context, PermissionRequest.DndAccess))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.surface,
                        colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            ),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            HeaderSection()
        }

        item {
            NotificationToggleCard(
                title = stringResource(R.string.show_battery_alerts),
                description = stringResource(R.string.show_battery_alerts_description),
                isChecked = showBatteryAlerts,
                onToggle = viewModel::toggleNotification,
                enabled = isMonitoringEnabled
            )
        }

        item {
            NotificationToggleCard(
                title = stringResource(R.string.show_battery_alerts_dnd),
                description = stringResource(R.string.show_battery_alerts_dnd_description),
                isChecked = isDNDEnabled,
                onToggle = viewModel::toggleDND,
                enabled = showBatteryAlerts
            )
        }

        item {
            BatteryAlertCard(
                title = stringResource(R.string.low_threshold_title),
                icon = ImageVector.vectorResource(id = R.drawable.round_battery_alert_24),
                iconColor = colorScheme.error,
                steps = lowSteps,
                valueRange = lowRange,
                triggers = lowThreshold,
                onTriggerUpdate = viewModel::updateLowThreshold,
                enabled = showBatteryAlerts,
                onAddTrigger = viewModel::addLowThreshold,
                onDeleted = viewModel::removeLowThreshold
            )
        }

        item {
            BatteryAlertCard(
                title = stringResource(R.string.high_threshold_title),
                icon = ImageVector.vectorResource(id = R.drawable.round_battery_charging_full_24),
                iconColor = Color(0xFF4CAF50),
                steps = highSteps,
                valueRange = highRange,
                triggers = highThreshold,
                onTriggerUpdate = viewModel::updateHighThreshold,
                enabled = showBatteryAlerts,
                onAddTrigger = viewModel::addHighThreshold,
                onDeleted = viewModel::removeHighThreshold,
                isHighAlerts = true
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HeaderSection() {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun NotificationToggleCard(
    title: String,
    description: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface,
            contentColor = if (enabled) colorScheme.onSurface else colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier.weight(0.2f),
                contentAlignment = Alignment.CenterEnd
            ){
                Switch(
                    modifier = Modifier.padding(end = 12.dp),
                    checked = isChecked,
                    enabled = enabled,
                    onCheckedChange = onToggle,
                )
            }
        }
    }
}

@Composable
private fun BatteryAlertCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    steps: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    triggers: List<Int>,
    onTriggerUpdate: (Int, Int) -> Unit,
    enabled: Boolean,
    onAddTrigger: (Int) -> Unit,
    onDeleted: (Int) -> Unit,
    isHighAlerts: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    var isAddTriggerSectionOpen by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .animateContentSize(
                animationSpec = tween(300)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) colorScheme.surface else colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (enabled) iconColor else colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (enabled) colorScheme.onSurface else colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalIconButton(
                    onClick = { isAddTriggerSectionOpen = !isAddTriggerSectionOpen },
                    enabled = enabled,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.add_battery_trigger)
                    )
                }
            }

            //Spacer(modifier = Modifier.height(16.dp))

            // Add trigger section
            AnimatedVisibility(
                visible = isAddTriggerSectionOpen,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                AddBatteryTriggerSection(
                    steps = steps,
                    valueRange = valueRange,
                    enabled = enabled,
                    onSavedTrigger = {
                        onAddTrigger(it)
                        isAddTriggerSectionOpen = false
                    }
                )
            }

            // Triggers list
            triggers.forEach { trigger ->
                Spacer(modifier = Modifier.height(12.dp))
                BatteryTriggerItem(
                    title = stringResource(if (isHighAlerts) R.string.high_trigger_name else R.string.low_trigger_name),
                    value = trigger,
                    steps = steps,
                    valueRange = valueRange,
                    onUpdate = onTriggerUpdate,
                    enabled = enabled,
                    onDeleted = { onDeleted(trigger) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatteryTriggerItem(
    title: String,
    value: Int,
    steps: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    onUpdate: (Int, Int) -> Unit,
    onDeleted: () -> Unit = {},
    enabled: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme
    var isEditMode by remember { mutableStateOf(false) }
    var trigger by remember { mutableIntStateOf(value) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(300)
            ),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        indication = ripple(),
                        interactionSource = remember { MutableInteractionSource() }
                    ) { isEditMode = !isEditMode }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$title: $value%",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (enabled) colorScheme.onSurface else colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = onDeleted,
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.delete_battery_trigger),
                        tint = if (enabled) colorScheme.error else colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isEditMode,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.update_battery_trigger) + ": ${trigger}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = trigger.toFloat(),
                        onValueChange = { trigger = it.roundToInt() },
                        valueRange = valueRange,
                        onValueChangeFinished = { onUpdate(value, trigger) },
                        steps = steps,
                        enabled = enabled,
                        colors = SliderDefaults.colors(
                            thumbColor = colorScheme.primary,
                            activeTrackColor = colorScheme.primary,
                            inactiveTrackColor = colorScheme.outline
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AddBatteryTriggerSection(
    steps: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean = true,
    onSavedTrigger: (Int) -> Unit = {}
) {
    val defaultValue = 15
    var trigger by remember { mutableIntStateOf(defaultValue) }
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.new_battery_trigger) + ": ${trigger}%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = trigger.toFloat(),
                onValueChange = { trigger = it.roundToInt() },
                valueRange = valueRange,
                steps = steps,
                enabled = enabled,
                colors = SliderDefaults.colors(
                    thumbColor = colorScheme.primary,
                    activeTrackColor = colorScheme.primary,
                    inactiveTrackColor = colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onSavedTrigger(trigger)
                    trigger = defaultValue
                },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.save_battery_trigger),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}