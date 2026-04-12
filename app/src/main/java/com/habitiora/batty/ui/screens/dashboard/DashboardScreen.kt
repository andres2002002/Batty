package com.habitiora.batty.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitiora.batty.domain.model.BatteryInfo
import com.habitiora.batty.domain.model.ServiceState
import com.habitiora.batty.services.PermissionRequest
import com.habitiora.batty.ui.components.RequestPermission
import com.habitiora.batty.ui.screens.dashboard.components.DetailsCard
import com.habitiora.batty.ui.screens.dashboard.components.ElectricalCard
import com.habitiora.batty.ui.screens.dashboard.components.HealthCard
import com.habitiora.batty.ui.screens.dashboard.components.LevelCard
import com.habitiora.batty.ui.screens.dashboard.components.MonitorToggleCard
import com.habitiora.batty.ui.screens.dashboard.components.SystemStateCard
import com.habitiora.batty.ui.utils.BatteryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState is BatteryUiState.Success && (uiState as BatteryUiState.Success).isMonitoring){
        RequestPermission(
            request = PermissionRequest.NotificationAccess,
            onGranted = { viewModel.setMonitorBattery(true)},
            onDenied = { viewModel.setMonitorBattery(false)}
        )
    }
    when (val state = uiState) {
        is BatteryUiState.Loading -> CircularProgressIndicator()

        is BatteryUiState.Error -> Text(text = state.message)

        is BatteryUiState.Success -> DashboardContent(
            info = state.liveInfo,
            serviceState = state.serviceState,
            onToggleMonitor = { enabled ->
                viewModel.setMonitorBattery(enabled)
            },
            onRetry = viewModel::retryService
        )
    }
}

@Composable
private fun DashboardContent(
    info: BatteryInfo,
    serviceState: ServiceState,
    onToggleMonitor: (Boolean) -> Unit,
    onRetry: () -> Unit,
) {
    val showHealthCard = info.capacityHealthPercent > 0 || info.cycleCount > 0

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "monitor_toggle") {
            MonitorToggleCard(
                serviceState = serviceState,
                onRetry = onRetry,
                onToggle = onToggleMonitor,
            )
        }

        item(key = "level") {
            LevelCard(
                level = info.level,
                status = info.status,
                plugged = info.plugged,
                isCharging = info.isCharging,
                estimatedMinutesRemaining = info.estimatedMinutesRemaining,
            )
        }

        item(key = "details") {
            DetailsCard(
                temperatureCelsius = info.temperature,
                voltageMv = info.voltage,
                health = info.health,
                technology = info.technology,
            )
        }

        item(key = "electrical") {
            ElectricalCard(
                currentNowMa = info.currentNowMa,
                currentAvgMa = info.currentAvgMa,
                watts = info.watts,
                chargeCounterMah = info.chargeCounterMah,
                estimatedMinutesRemaining = info.estimatedMinutesRemaining,
                isCharging = info.isCharging,
            )
        }

        item(key = "system_state") {
            SystemStateCard(
                isScreenOn = info.isScreenOn,
                isBatterySaver = info.isBatterySaver,
                isDozeMode = info.isDozeMode,
            )
        }

        if (showHealthCard) {
            item(key = "health") {
                HealthCard(
                    capacityHealthPercent = info.capacityHealthPercent,
                    cycleCount = info.cycleCount,
                    fullCapacityMah = info.fullCapacityMah,
                    designCapacityMah = info.designCapacityMah,
                )
            }
        }
    }
}