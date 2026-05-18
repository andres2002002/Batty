package com.habitiora.batty.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.habitiora.batty.R
import com.habitiora.batty.domain.model.BatteryHealth
import com.habitiora.batty.ui.components.InfoRow
import com.habitiora.batty.ui.components.InfoRowEmphasis
import com.habitiora.batty.ui.components.SectionHeader
import com.habitiora.batty.ui.components.card.BattyCard
import com.habitiora.batty.ui.components.card.BattyCardDefaults
import com.habitiora.batty.ui.components.card.BattyCardVariant
import com.habitiora.batty.ui.utils.BatteryFormatter

/**
 * Card de detalles físicos de la batería.
 *
 * @param temperatureCelsius Temperatura en °C. <= 0 muestra "—".
 * @param health Estado físico de la batería.
 * @param technology Tecnología de celda. Null o blank muestra "—".
 * @param modifier Modifier externo.
 */
@Composable
fun DetailsCard(
    temperatureCelsius: Float,
    health: BatteryHealth,
    technology: String?,
    modifier: Modifier = Modifier,
) {
    BattyCard(
        variant = BattyCardVariant.Default,
        modifier = modifier,
        header = { SectionHeader(title = stringResource(R.string.dashboard_details_title)) },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TemperatureCard(
                temperatureCelsius = temperatureCelsius,
                modifier = Modifier.weight(1f),
            )
            TechnologyCard(
                technology = technology,
                modifier = Modifier.weight(1f),
            )
            BatteryHealthCard(
                health = health,
                modifier = Modifier.weight(1f),
            )
        }
    }
}