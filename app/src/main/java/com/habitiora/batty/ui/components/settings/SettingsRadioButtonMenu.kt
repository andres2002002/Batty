package com.habitiora.batty.ui.components.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Immutable
data class RadioButtonOption<T>(
    val value: T,
    val icon: ImageVector? = null,
    val label: String,
    val description: String? = null,
)

/**
 * Settings item con grupo de radio buttons expandible in-place.
 *
 * El estado de expansión es interno y sobrevive recomposiciones via [rememberSaveable].
 * El caller solo gestiona [selectedValue] y [onValueChange].
 *
 * @param T Tipo del valor seleccionable — típicamente un enum de dominio.
 * @param title Texto del header del item.
 * @param selectedValue Valor actualmente seleccionado.
 * @param options Lista de opciones. Ver [RadioButtonOption].
 * @param onValueChange Callback cuando el usuario selecciona una opción.
 * @param modifier Modifier externo.
 * @param description Subtítulo opcional en el header. Muestra el label de la opción
 *                    seleccionada si es null.
 * @param icon Ícono leading opcional en el header.
 * @param enabled Cuando false desactiva interacción y aplica alpha.
 */
@Composable
fun <T> SettingsRadioButtonMenu(
    title: String,
    selectedValue: T,
    options: List<RadioButtonOption<T>>,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    shape: Shape? = null,
    enabled: Boolean = true,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val resolvedDescription = description ?: options.find { it.value == selectedValue }?.label

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else SettingsItemDefaults.DisabledAlpha)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 250,
                    easing = FastOutSlowInEasing,
                ),
            ),
    ) {
        // Header row — clickable para expandir/colapsar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { role = Role.Switch }
                .clip(shape ?: MaterialTheme.shapes.medium)
                .clickable(
                    enabled = enabled,
                    onClick = { expanded = !expanded },
                )
                .padding(
                    horizontal = SettingsItemDefaults.HorizontalPadding,
                    vertical = SettingsItemDefaults.VerticalPadding,
                ),
            horizontalArrangement = Arrangement.spacedBy(SettingsItemDefaults.IconSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(SettingsItemDefaults.IconSize),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SettingsItemDefaults.DescriptionSpacing),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (resolvedDescription != null) {
                    Text(
                        text = resolvedDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Options — visibles solo cuando expanded
        if (expanded) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = SettingsItemDefaults.HorizontalPadding),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
                    .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
            ) {
                options.forEach { option ->
                    RadioButtonOptionRow(
                        option = option,
                        selected = option.value == selectedValue,
                        enabled = enabled,
                        onClick = {
                            onValueChange(option.value)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> RadioButtonOptionRow(
    option: RadioButtonOption<T>,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onClick,
            )
            .semantics { role = Role.RadioButton }
            .padding(
                start = SettingsItemDefaults.HorizontalPadding,
                end = SettingsItemDefaults.HorizontalPadding,
                top = 4.dp,
                bottom = 4.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(SettingsItemDefaults.IconSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null, // manejado por el Row clickable — evita doble ripple
            enabled = enabled,
        )
        option.icon?.let { icon ->
            Box(
                modifier = Modifier.size(SettingsItemDefaults.IconSize),
                contentAlignment = Alignment.Center,
            ){
                Icon(
                    modifier = Modifier.matchParentSize(),
                    imageVector = icon,
                    contentDescription = null,
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(SettingsItemDefaults.DescriptionSpacing),
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            if (option.description != null) {
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}