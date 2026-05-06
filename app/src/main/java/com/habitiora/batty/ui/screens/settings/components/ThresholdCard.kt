package com.habitiora.batty.ui.screens.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.habitiora.batty.ui.components.SectionHeader
import com.habitiora.batty.ui.components.card.BattyCard
import com.habitiora.batty.ui.components.card.BattyCardDefaults
import com.habitiora.batty.ui.components.card.BattyCardVariant
import com.habitiora.batty.ui.components.settings.SettingsItemDefaults
import kotlin.math.roundToInt

private val PremiumSpringSpec = spring(
    dampingRatio = 0.8f,
    stiffness = 400f,
    visibilityThreshold = IntOffset.VisibilityThreshold
)
private val FadeSpringSpec = spring<Float>(dampingRatio = 0.8f, stiffness = 400f)

@Composable
fun ThresholdCard(
    title: String,
    icon: ImageVector,
    thresholds: List<Int>,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    triggerLabel: String,
    enabled: Boolean,
    onAdd: (Int) -> Unit,
    onUpdate: (Int, Int) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var addPanelVisible by rememberSaveable { mutableStateOf(false) }

    BattyCard(
        variant = BattyCardVariant.Default,
        modifier = modifier.animateContentSize(spring(dampingRatio = 0.8f, stiffness = 400f)),
        header = {
            ThresholdCardHeader(
                title = title,
                icon = icon,
                enabled = enabled,
                addPanelOpen = addPanelVisible,
                onToggleAddPanel = { addPanelVisible = !addPanelVisible },
            )
        },
        contentPadding = BattyCardDefaults.ContentPadding.copy(top = 0.dp),
    ) {
        AnimatedVisibility(
            visible = addPanelVisible,
            enter = slideInVertically(
                initialOffsetY = { -it / 3 },
                animationSpec = PremiumSpringSpec,
            ) + fadeIn(FadeSpringSpec),
            exit = slideOutVertically(
                targetOffsetY = { -it / 3 },
                animationSpec = PremiumSpringSpec,
            ) + fadeOut(FadeSpringSpec),
        ) {
            AddThresholdPanel(
                valueRange = valueRange,
                steps = steps,
                enabled = enabled,
                onSave = { value ->
                    onAdd(value)
                    addPanelVisible = false
                },
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = if (addPanelVisible) 8.dp else 0.dp)
        ) {
            if (thresholds.isEmpty()) {
                EmptyThresholdsHint()
            } else {
                thresholds.forEach { threshold ->
                    ThresholdItem(
                        value = threshold,
                        label = triggerLabel,
                        valueRange = valueRange,
                        steps = steps,
                        enabled = enabled,
                        onUpdate = { new -> onUpdate(threshold, new) },
                        onDelete = { onDelete(threshold) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThresholdCardHeader(
    title: String,
    icon: ImageVector,
    enabled: Boolean,
    addPanelOpen: Boolean,
    onToggleAddPanel: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .alpha(if (enabled) 1f else SettingsItemDefaults.DisabledAlpha),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            SectionHeader(title = title)
        }

        FilledTonalIconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggleAddPanel()
            },
            enabled = enabled,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (addPanelOpen) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                contentColor = if (addPanelOpen) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                },
            ),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = if (addPanelOpen) "Close add panel" else "Add threshold"
            )
        }
    }
}

@Composable
private fun AddThresholdPanel(
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    enabled: Boolean,
    onSave: (Int) -> Unit,
) {
    val defaultValue = valueRange.start.roundToInt()
    var sliderValue by remember { mutableIntStateOf(defaultValue) }
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "New threshold",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "$sliderValue%",
                // Reducido a titleMedium para balance
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Slider(
            value = sliderValue.toFloat(),
            onValueChange = {
                val newValue = it.roundToInt()
                if (newValue != sliderValue) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                sliderValue = newValue
            },
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
        )

        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSave(sliderValue)
                sliderValue = defaultValue
            },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Save threshold", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ThresholdItem(
    value: Int,
    label: String,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    enabled: Boolean,
    onUpdate: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    var editMode by rememberSaveable(value) { mutableStateOf(false) }
    var sliderValue by remember(value) { mutableIntStateOf(value) }
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (editMode) 0.5f else 0.2f))
            .animateContentSize(spring(dampingRatio = 0.8f, stiffness = 400f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .semantics { role = Role.Button }
                .clickable(enabled = enabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    editMode = !editMode
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "$value%",
                    // Reducido de headlineMedium a titleLarge
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                },
                enabled = enabled,
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete threshold",
                    tint = if (enabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(
            visible = editMode,
            enter = slideInVertically(
                initialOffsetY = { -it / 3 },
                animationSpec = PremiumSpringSpec,
            ) + fadeIn(FadeSpringSpec),
            exit = slideOutVertically(
                targetOffsetY = { -it / 3 },
                animationSpec = PremiumSpringSpec,
            ) + fadeOut(FadeSpringSpec),
        ) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Adjust value",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "$sliderValue%",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Slider(
                    value = sliderValue.toFloat(),
                    onValueChange = {
                        val newValue = it.roundToInt()
                        if (newValue != sliderValue) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        sliderValue = newValue
                    },
                    valueRange = valueRange,
                    onValueChangeFinished = { onUpdate(sliderValue) },
                    steps = steps,
                    enabled = enabled,
                )
            }
        }
    }
}

@Composable
private fun EmptyThresholdsHint() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No thresholds configured.\nTap + to add one.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private fun androidx.compose.foundation.layout.PaddingValues.copy(
    top: androidx.compose.ui.unit.Dp = this.calculateTopPadding(),
    bottom: androidx.compose.ui.unit.Dp = this.calculateBottomPadding(),
): androidx.compose.foundation.layout.PaddingValues =
    androidx.compose.foundation.layout.PaddingValues(
        start = this.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
        top = top,
        end = this.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
        bottom = bottom,
    )