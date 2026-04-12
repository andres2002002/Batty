package com.habitiora.batty.ui.screens.settings.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.habitiora.batty.R
import com.habitiora.batty.domain.model.AlertPolicy
import com.habitiora.batty.ui.components.SectionHeader
import com.habitiora.batty.ui.components.card.BattyCard
import com.habitiora.batty.ui.components.card.BattyCardDefaults
import com.habitiora.batty.ui.components.card.BattyCardVariant
import com.habitiora.batty.ui.components.settings.RadioButtonOption
import com.habitiora.batty.ui.components.settings.SettingsRadioButtonMenu

/**
 * Card de selección para una [AlertPolicy]. Clickable en toda su superficie.
 * Seleccionado → [BattyCardVariant.Outlined] con borde primary.
 * No seleccionado → [BattyCardVariant.Default].
 */
@Composable
fun AlertPolicyCard(
    policy: AlertPolicy,
    onValueChange: (AlertPolicy) -> Unit,
    modifier: Modifier = Modifier,
) {
    BattyCard(
        variant = BattyCardVariant.Default,
        modifier = modifier.semantics { role = Role.RadioButton },
        contentPadding = BattyCardDefaults.SoftPadding,
    ) {
        SettingsRadioButtonMenu(
            title = stringResource(policy.titleRes),
            description = stringResource(policy.descriptionRes),
            selectedValue = policy,
            options = AlertPolicy.entries.map {
                RadioButtonOption(
                    value = it,
                    icon = it.icon,
                    label = stringResource(it.titleRes),
                    description = stringResource(it.descriptionRes),
                )
            },
            onValueChange = onValueChange,
            icon = policy.icon,
        )
    }
}