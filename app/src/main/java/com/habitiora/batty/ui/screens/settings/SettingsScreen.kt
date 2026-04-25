package com.habitiora.batty.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitiora.batty.R
import com.habitiora.batty.domain.model.AlertPolicy
import com.habitiora.batty.domain.model.DndBypassState
import com.habitiora.batty.domain.model.MonitorSettings
import com.habitiora.batty.domain.model.ThresholdsConfig
import com.habitiora.batty.services.NotificationHelper
import com.habitiora.batty.ui.components.SectionHeader
import com.habitiora.batty.ui.components.card.BattyCard
import com.habitiora.batty.ui.components.card.BattyCardDefaults
import com.habitiora.batty.ui.components.card.BattyCardVariant
import com.habitiora.batty.ui.components.settings.SettingsToggleItem
import com.habitiora.batty.ui.screens.settings.components.AlertPolicyCard
import com.habitiora.batty.ui.screens.settings.components.DndBypassItem
import com.habitiora.batty.ui.screens.settings.components.ThresholdCard
import com.habitiora.batty.ui.utils.SettingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Refresca DndBypassState en cada ON_RESUME — la fuente de verdad
    // es NotificationChannel.canBypassDnd(), que solo cambia cuando el
    // usuario sale a settings del sistema y vuelve.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                //viewModel.refreshDndBypassState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when (val state = uiState) {
        is SettingsUiState.Loading -> CircularProgressIndicator()

        is SettingsUiState.Success -> SettingsContent(
            settings = state.settings,
            thresholds = state.thresholds,
            dndBypassState = state.dndBypass,
            viewModel = viewModel,
        )
    }

}

@Composable
private fun SettingsContent(
    settings: MonitorSettings,
    thresholds: ThresholdsConfig,
    dndBypassState: DndBypassState,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // ── Alerts ───────────────────────────────────────────────────────────

        item(key = "header_alerts") {
            SectionHeader(
                title = stringResource(R.string.alert_policy_title),
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )
        }

        item(key = "alert_policy_card") {
            AlertPolicyCard(
                policy = settings.alertPolicy,
                onValueChange = viewModel::setAlertPolicy,
            )
        }

        // ── Thresholds ───────────────────────────────────────────────────────

        item(key = "header_thresholds") {
            SectionHeader(
                title = "Thresholds",
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
        }

        item(key = "threshold_low") {
            ThresholdCard(
                title = "Low battery",
                icon = Icons.Outlined.BatteryAlert,
                thresholds = thresholds.lowThresholds.sorted(),
                valueRange = 1f..49f,
                steps = 47,  // pasos entre 1 y 49 sin extremos = 47
                triggerLabel = "Alert at",
                enabled = settings.alertPolicy.isNotDisabled,
                onAdd = viewModel::addLowThreshold,
                onUpdate = viewModel::updateLowThreshold,
                onDelete = viewModel::removeLowThreshold,
            )
        }

        item(key = "threshold_high") {
            ThresholdCard(
                title = "High battery",
                icon = Icons.Outlined.BatteryChargingFull,
                thresholds = thresholds.highThresholds.sortedDescending(),
                valueRange = 51f..100f,
                steps = 48,  // pasos entre 51 y 100 sin extremos = 48
                triggerLabel = "Alert at",
                enabled = settings.alertPolicy.isNotDisabled,
                onAdd = viewModel::addHighThreshold,
                onUpdate = viewModel::updateHighThreshold,
                onDelete = viewModel::removeHighThreshold,
            )
        }

        // ── General ──────────────────────────────────────────────────────────

        item(key = "header_general") {
            SectionHeader(
                title = "General",
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
        }

        item(key = "general_card") {
            BattyCard(
                variant = BattyCardVariant.Default,
                contentPadding = BattyCardDefaults.ContentPadding,
            ) {
                SettingsToggleItem(
                    title = "Start on boot",
                    description = "Automatically start monitoring after device restart",
                    checked = settings.startOnBoot,
                    onCheckedChange = viewModel::setStartOnBoot,
                    icon = Icons.Outlined.PowerSettingsNew,
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )

                DndBypassItem(
                    dndBypassState = dndBypassState,
                    channelId = NotificationHelper.CRITICAL_CHANNEL_ID,
                )
            }
        }
    }
}