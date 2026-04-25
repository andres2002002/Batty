package com.habitiora.batty.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitiora.batty.domain.model.BatteryInfo
import com.habitiora.batty.domain.model.ServiceState
import com.habitiora.batty.services.PermissionRequest
import com.habitiora.batty.ui.components.RequestPermission
import com.habitiora.batty.ui.screens.dashboard.components.BatteryHealthCard
import com.habitiora.batty.ui.screens.dashboard.components.DetailsCard
import com.habitiora.batty.ui.screens.dashboard.components.ElectricalCard
import com.habitiora.batty.ui.screens.dashboard.components.HealthCard
import com.habitiora.batty.ui.screens.dashboard.components.LevelCard
import com.habitiora.batty.ui.screens.dashboard.components.MonitorToggleCard
import com.habitiora.batty.ui.screens.dashboard.components.SystemStateCard
import com.habitiora.batty.ui.screens.dashboard.components.TechnologyCard
import com.habitiora.batty.ui.screens.dashboard.components.TemperatureCard
import com.habitiora.batty.ui.utils.BatteryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        is BatteryUiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        is BatteryUiState.Error -> Text(text = state.message)

        is BatteryUiState.Success -> DashboardContent(
            isMonitoring = state.isMonitoring,
            info = state.liveInfo,
            serviceState = state.serviceState,
            onRetry = viewModel::retryService,
            setMonitorBattery = viewModel::setMonitorBattery,
        )
    }
}

@Composable
private fun DashboardContent(
    isMonitoring: Boolean,
    info: BatteryInfo,
    serviceState: ServiceState,
    onRetry: () -> Unit,
    setMonitorBattery: (Boolean) -> Unit,
) {
    val showHealthCard = info.capacityHealthPercent > 0 || info.cycleCount > 0

    if (isMonitoring){
        RequestPermission(
            request = PermissionRequest.NotificationAccess,
            onGranted = { setMonitorBattery(true)},
            onDenied = { setMonitorBattery(false)}
        )
    }
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
                onToggle = setMonitorBattery,
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
                health = info.health,
                technology = info.technology,
            )
        }

        item(key = "electrical") {
            ElectricalCard(
                voltageMv = info.voltage,
                currentNowMa = info.currentNowMa,
                currentAvgMa = info.currentAvgMa,
                watts = info.watts,
                chargeCounterMah = info.chargeCounterMah,
                fullCapacityMah = info.fullCapacityMah,
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