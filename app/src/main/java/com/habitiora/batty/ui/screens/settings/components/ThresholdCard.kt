package com.habitiora.batty.ui.screens.settings.components

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.habitiora.batty.ui.components.SectionHeader
import com.habitiora.batty.ui.components.card.BattyCard
import com.habitiora.batty.ui.components.card.BattyCardDefaults
import com.habitiora.batty.ui.components.card.BattyCardVariant
import com.habitiora.batty.ui.components.settings.SettingsItemDefaults
import kotlin.math.roundToInt

private val SlideAnimSpec: FiniteAnimationSpec<IntOffset> = tween(durationMillis = 200, easing = FastOutSlowInEasing)
private val FadeAnimSpec = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)

/**
 * Card de gestión de thresholds — bajo o alto.
 *
 * Permite añadir, editar (slider inline) y eliminar thresholds.
 * El estado de expansión del panel "añadir" y de edición por item
 * son internos y sobreviven recomposiciones via [rememberSaveable].
 *
 * @param title Título del card.
 * @param icon Ícono leading del header.
 * @param thresholds Lista actual de thresholds del tipo correspondiente.
 * @param valueRange Rango válido: 1f..49f para low, 51f..100f para high.
 * @param steps Número de pasos del slider.
 * @param triggerLabel Label del item de threshold ("Alert at" etc).
 * @param enabled Cuando false desactiva toda interacción.
 * @param onAdd Callback al confirmar nuevo threshold.
 * @param onUpdate Callback al modificar un threshold existente (old, new).
 * @param onDelete Callback al eliminar un threshold.
 */
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
        modifier = modifier.animateContentSize(tween(300, easing = FastOutSlowInEasing)),
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
        // Panel añadir threshold
        AnimatedVisibility(
            visible = addPanelVisible,
            enter = slideInVertically(
                initialOffsetY = { -it / 2 },
                animationSpec = SlideAnimSpec,
            ) + fadeIn(FadeAnimSpec),
            exit = slideOutVertically(
                targetOffsetY = { -it / 2 },
                animationSpec = SlideAnimSpec,
            ) + fadeOut(FadeAnimSpec),
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

        if (thresholds.isEmpty()) {
            EmptyThresholdsHint()
        } else {
            thresholds.forEachIndexed { index, threshold ->
                if (index > 0) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                }
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

@Composable
private fun ThresholdCardHeader(
    title: String,
    icon: ImageVector,
    enabled: Boolean,
    addPanelOpen: Boolean,
    onToggleAddPanel: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else SettingsItemDefaults.DisabledAlpha),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SectionHeader(title = title)
        }

        FilledTonalIconButton(
            onClick = onToggleAddPanel,
            enabled = enabled,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (addPanelOpen) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                contentColor = if (addPanelOpen) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                },
            ),
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = if (addPanelOpen) "Close add panel" else "Add threshold",
                modifier = Modifier.size(18.dp),
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "New threshold",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$sliderValue%",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Slider(
            value = sliderValue.toFloat(),
            onValueChange = { sliderValue = it.roundToInt() },
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        )

        Button(
            onClick = {
                onSave(sliderValue)
                sliderValue = defaultValue
            },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Add threshold")
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(tween(200, easing = FastOutSlowInEasing)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { role = Role.Button }
                .clickable(enabled = enabled) { editMode = !editMode }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "$value%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            IconButton(
                onClick = onDelete,
                enabled = enabled,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete threshold",
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = editMode,
            enter = slideInVertically(
                initialOffsetY = { -it / 2 },
                animationSpec = SlideAnimSpec,
            ) + fadeIn(FadeAnimSpec),
            exit = slideOutVertically(
                targetOffsetY = { -it / 2 },
                animationSpec = SlideAnimSpec,
            ) + fadeOut(FadeAnimSpec),
        ) {
            Column(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Adjust threshold",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "$sliderValue%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Slider(
                    value = sliderValue.toFloat(),
                    onValueChange = { sliderValue = it.roundToInt() },
                    valueRange = valueRange,
                    onValueChangeFinished = { onUpdate(sliderValue) },
                    steps = steps,
                    enabled = enabled,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
            }
        }
    }
}

@Composable
private fun EmptyThresholdsHint() {
    Text(
        text = "No thresholds configured. Tap + to add one.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

// Extension para copy en PaddingValues — mismo helper del Dashboard
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