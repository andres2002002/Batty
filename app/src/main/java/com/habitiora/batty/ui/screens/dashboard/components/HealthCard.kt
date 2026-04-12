package com.habitiora.batty.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habitiora.batty.ui.components.InfoRow
import com.habitiora.batty.ui.components.InfoRowEmphasis
import com.habitiora.batty.ui.components.SectionHeader
import com.habitiora.batty.ui.components.battery.BatteryLevelBar
import com.habitiora.batty.ui.components.battery.BatteryLevelBarSize
import com.habitiora.batty.ui.components.card.BattyCard
import com.habitiora.batty.ui.components.card.BattyCardVariant
import com.habitiora.batty.ui.utils.BatteryFormatter

/**
 * Card de salud de la batería. Muestra capacidad actual vs diseño y ciclos.
 *
 * **El caller es responsable de decidir si renderiza este card.**
 * Condición del Dashboard: `capacityHealthPercent > 0 || cycleCount > 0`.
 *
 * @param capacityHealthPercent Porcentaje de salud de capacidad. -1 si no disponible.
 * @param cycleCount Ciclos de carga completados. -1 si no disponible.
 * @param fullCapacityMah Capacidad real actual en mAh. -1 si no disponible.
 * @param designCapacityMah Capacidad de diseño original en mAh. -1 si no disponible.
 * @param modifier Modifier externo.
 */
@Composable
fun HealthCard(
    capacityHealthPercent: Int,
    cycleCount: Int,
    fullCapacityMah: Int,
    designCapacityMah: Int,
    modifier: Modifier = Modifier,
) {
    BattyCard(
        variant = BattyCardVariant.Default,
        modifier = modifier,
        header = { SectionHeader(title = "Battery health") },
    ) {
        // Barra de salud — solo si hay dato
        if (capacityHealthPercent > 0) {
            HealthBar(capacityHealthPercent = capacityHealthPercent)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Porcentaje de salud
        InfoRow(
            label = "Capacity health",
            value = if (capacityHealthPercent > 0) "$capacityHealthPercent%" else null,
            emphasis = InfoRowEmphasis.ValueHighlighted,
            valueColor = healthPercentColor(capacityHealthPercent),
        )

        HealthCardDivider()

        InfoRow(
            label = "Current capacity",
            value = BatteryFormatter.chargeCounter(fullCapacityMah),
            emphasis = InfoRowEmphasis.Default,
        )

        HealthCardDivider()

        InfoRow(
            label = "Design capacity",
            value = BatteryFormatter.chargeCounter(designCapacityMah),
            emphasis = InfoRowEmphasis.Muted,
        )

        HealthCardDivider()

        InfoRow(
            label = "Cycle count",
            value = if (cycleCount > 0) cycleCount.toString() else null,
            emphasis = InfoRowEmphasis.Default,
        )
    }
}

/**
 * Barra de nivel reutilizada para representar salud de capacidad.
 * El color funcional ya cubre la semántica: verde/amarillo/rojo
 * aplica igual para nivel de batería y para salud de capacidad.
 */
@Composable
private fun HealthBar(capacityHealthPercent: Int) {
    BatteryLevelBar(
        level = capacityHealthPercent.coerceIn(0, 100),
        size = BatteryLevelBarSize.Small,
    )
}

/**
 * Misma escala semántica que [batteryLevelColor] aplicada a capacidad:
 * >= 80% → primary | 50–79% → tertiary | < 50% → error
 */
@Composable
private fun healthPercentColor(percent: Int) = when {
    percent <= 0 -> MaterialTheme.colorScheme.onSurfaceVariant
    percent >= 80 -> MaterialTheme.colorScheme.primary
    percent >= 50 -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.error
}

@Composable
private fun HealthCardDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}