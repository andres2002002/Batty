package com.habitiora.batty.ui.screens.main

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitiora.batty.R
import com.habitiora.batty.domain.model.BatteryState
import com.habitiora.batty.services.PermissionRequest
import com.habitiora.batty.services.PermissionsHelper
import com.habitiora.batty.ui.components.RequestPermission
import com.habitiora.batty.utils.toDecimalFormat

@Composable
fun BatteryMainInfo(
    modifier: Modifier = Modifier,
    viewModel: BatteryInfoViewModel = hiltViewModel()
) {
    val state by viewModel.batteryState.collectAsState()
    val isMonitoring by viewModel.isMonitoring.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    if (isMonitoring){
        RequestPermission(
            request = PermissionRequest.NotificationAccess,
            onGranted = { viewModel.startBatteryMonitorService(context)},
            onDenied = { viewModel.toggleMonitoring(false)}
        )
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
            BatteryLevelCard(
                batteryLevel = state?.batteryLevel ?: 0,
                isCharging = state?.isCharging ?: false,
                isMonitoring = isMonitoring,
                chargingType = stringResource(state?.chargingType?.nameId ?: R.string.status_unknown) ,
                onMonitoringChange = viewModel::toggleMonitoring
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TemperatureCard(
                    temperature = state?.temperature ?: -1,
                    modifier = Modifier.weight(1f)
                )
                VoltageCard(
                    voltage = state?.voltage ?: -1,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            TechnicalInfoCard(state = state)
        }

        item {
            SystemStatusCard(state = state)
        }

        if (state?.chargingRate != null || state?.dischargeRate != null || state?.estimatedTimeRemaining != null) {
            item {
                StatisticsCard(state = state)
            }
        }
    }
}

@Composable
private fun BatteryLevelCard(
    batteryLevel: Int,
    isCharging: Boolean,
    isMonitoring: Boolean,
    chargingType: String,
    onMonitoringChange: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val batteryColor = getBatteryColor(batteryLevel, isCharging)
    val statusIcon: Int = remember(isCharging) {
        if (isCharging) R.drawable.round_power_24 else R.drawable.round_battery_std_24
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MonitoringStatus(
                isMonitoring = isMonitoring,
                onStatusChange = onMonitoringChange
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Indicador circular de batería
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                BatteryCircularIndicator(
                    batteryLevel = batteryLevel,
                    isCharging = isCharging,
                    color = batteryColor
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${batteryLevel}%",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp
                        ),
                        color = colorScheme.onPrimaryContainer
                    )

                    if (isCharging) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.round_bolt_24),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFFC107)
                            )
                            Text(
                                text = stringResource(id = R.string.status_charging),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Estado de carga
            val statusText = if (isCharging)
                stringResource(R.string.connected) + " - " + chargingType
            else
                stringResource(R.string.disconnected)
            StatusChip(
                text = statusText,
                icon = ImageVector.vectorResource(id = statusIcon),
                containerColor = if (isCharging) Color(0xFF4CAF50) else colorScheme.outline,
                contentColor = if (isCharging) Color.White else colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BatteryCircularIndicator(
    batteryLevel: Int,
    isCharging: Boolean,
    color: Color
) {
    val animatedProgress by animateFloatAsState(
        targetValue = batteryLevel / 100f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "battery_progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "charging_animation")
    val chargingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "charging_alpha"
    )

    Canvas(modifier = Modifier.size(200.dp)) {
        val strokeWidth = 12.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Fondo del círculo
        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // Progreso de la batería
        drawArc(
            color = if (isCharging) color.copy(alpha = chargingAlpha) else color,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            topLeft = Offset(
                center.x - radius,
                center.y - radius
            ),
            size = Size(radius * 2, radius * 2),
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            )
        )
    }
}

@Composable
private fun MonitoringStatus(
    isMonitoring: Boolean,
    onStatusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
){
    val statusText = if (isMonitoring) R.string.monitoring_on else R.string.monitoring_off
    val colorScheme = MaterialTheme.colorScheme
    val textColor = if (isMonitoring) colorScheme.onPrimaryContainer else colorScheme.onSurface
    val statusIcon = if (isMonitoring) R.drawable.batty_icon_open else R.drawable.batty_icon_close
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Text(
            text = stringResource(id = statusText),
            color = textColor,
            style = MaterialTheme.typography.titleMedium
        )
        Switch(isMonitoring, onStatusChange, thumbContent = {
            Icon(
                modifier = Modifier.size(SwitchDefaults.IconSize),
                imageVector = ImageVector.vectorResource(statusIcon),
                contentDescription = null
            )
        })
    }
}
@Composable
private fun TemperatureCard(
    temperature: Int,
    modifier: Modifier = Modifier
) {
    val tempCelsius = if (temperature > 0) temperature / 10f else null
    val colorScheme = MaterialTheme.colorScheme
    val locale = Locale.current.platformLocale
    val tempColor = when {
        tempCelsius == null -> colorScheme.onSurface
        tempCelsius > 45 -> Color(0xFFFF5722)
        tempCelsius > 35 -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    val subtitle = when {
        tempCelsius == null -> R.string.temperature_not_available
        tempCelsius > 45 -> R.string.temperature_very_warm
        tempCelsius > 35 -> R.string.temperature_warm
        tempCelsius < 10 -> R.string.temperature_cold
        else -> R.string.temperature_normal
    }
    InfoCard(
        modifier = modifier,
        title = stringResource(id = R.string.temperature_title),
        value = tempCelsius?.let { "${it.toDecimalFormat(locale, 1)}°C" } ?: "--",
        icon = ImageVector.vectorResource(id = R.drawable.round_thermostat_24),
        iconColor = tempColor,
        subtitle = stringResource(id = subtitle)
    )
}

@Composable
private fun VoltageCard(
    voltage: Int,
    modifier: Modifier = Modifier
) {
    val voltageV = if (voltage > 0) voltage / 1000f else null
    val locale = Locale.current.platformLocale

    val subtitle = when {
        voltageV == null -> R.string.voltage_not_available
        voltageV > 4.2 -> R.string.voltage_high
        voltageV > 3.3 -> R.string.voltage_normal
        else -> R.string.voltage_low
    }
    InfoCard(
        modifier = modifier,
        title = stringResource(id = R.string.voltage_title),
        value = voltageV?.let { "${it.toDecimalFormat(locale)}V" } ?: "--",
        icon = ImageVector.vectorResource(id = R.drawable.round_electric_bolt_24),
        iconColor = Color(0xFF2196F3),
        subtitle = stringResource(id = subtitle)
    )
}

@Composable
private fun TechnicalInfoCard(state: BatteryState?) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.round_memory_24),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.technical_info_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TechnicalInfoItem(
                    label = stringResource(id = R.string.current_title),
                    value = state?.current?.let { "${it/1000}mA" } ?: "--",
                    modifier = Modifier.weight(1f)
                )
                TechnicalInfoItem(
                    label = stringResource(id = R.string.current_capacity),
                    value = state?.capacity?.let { "${it / 1000}mAh" } ?: "--",
                    modifier = Modifier.weight(1f)
                )
                TechnicalInfoItem(
                    label = stringResource(id = R.string.health_device),
                    value = state?.batteryHealth?.nameId?.let { stringResource(id = it) } ?: "--",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SystemStatusCard(state: BatteryState?) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.round_smartphone_24),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.system_status_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusIndicator(
                    label = stringResource(id = R.string.screen_status),
                    isActive = state?.screenOn ?: false,
                    modifier = Modifier.weight(1f)
                )
                StatusIndicator(
                    label = stringResource(id = R.string.energy_save_mode),
                    isActive = state?.powerSaveMode ?: false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatisticsCard(state: BatteryState?) {
    val colorScheme = MaterialTheme.colorScheme
    val locale = Locale.current.platformLocale

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.outline_analytics_24),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.statistics_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state?.chargingRate?.let { rate ->
                    StatisticItem(
                        label = stringResource(R.string.statistics_charging_rate),
                        value = "${rate.toDecimalFormat(locale, 1)}}%/h",
                        icon = ImageVector.vectorResource(id = R.drawable.outline_trending_up_24)
                    )
                }

                state?.dischargeRate?.let { rate ->
                    StatisticItem(
                        label = stringResource(R.string.statistics_discharge_rate),
                        value = "${rate.toDecimalFormat(locale, 1)}%/h",
                        icon = ImageVector.vectorResource(id = R.drawable.round_trending_down_24)                    )
                }

                state?.estimatedTimeRemaining?.let { time ->
                    val hours = time / 60
                    val minutes = time % 60
                    StatisticItem(
                        label = stringResource(R.string.statistics_estimated_time_remaining),
                        value = "${hours}h ${minutes}m",
                        icon = ImageVector.vectorResource(id = R.drawable.round_schedule_24)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    subtitle: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = iconColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.shadow(4.dp, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun TechnicalInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatusIndicator(
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = if (isActive) Color(0xFF4CAF50) else colorScheme.outline,
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurface
        )
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    val colorScheme = MaterialTheme.colorScheme

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
                modifier = Modifier.size(20.dp),
                tint = colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = colorScheme.onSurface
        )
    }
}

private fun getBatteryColor(batteryLevel: Int, isCharging: Boolean): Color {
    return when {
        isCharging -> Color(0xFF4CAF50)
        batteryLevel > 60 -> Color(0xFF4CAF50)
        batteryLevel > 30 -> Color(0xFFFF9800)
        batteryLevel > 15 -> Color(0xFFFF5722)
        else -> Color(0xFFD32F2F)
    }
}