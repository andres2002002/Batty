package com.habitiora.batty.ui.screens.dashboard.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.habitiora.batty.data.model.ColorUI
import com.habitiora.batty.ui.components.InfoColumn
import com.habitiora.batty.ui.components.InfoColumnDefaults
import com.habitiora.batty.ui.components.InfoRowEmphasis
import com.habitiora.batty.ui.utils.BatteryFormatter

@Composable
fun TechnologyCard(
    modifier: Modifier = Modifier,
    technology: String?,
) {
    InfoColumn(
        modifier = modifier,
        label = "Technology",
        value = BatteryFormatter.technology(technology),
        icon = {
            Icon(
                modifier = Modifier.size(InfoColumnDefaults.IconSize.Large),
                imageVector = Icons.Outlined.Memory,
                tint = ColorUI.Blue.color,
                contentDescription = null
            )
        },
        emphasis = InfoRowEmphasis.Default,
        valueColor = technologyColor(technology),
    )

}


@Composable
private fun technologyColor(technology: String?): Color {
    val tech = technology?.trim()?.lowercase() ?: return MaterialTheme.colorScheme.onSurfaceVariant

    return when {
        // Tecnologías estándar modernas (Verde/Primario)
        tech.contains("li-ion") || tech.contains("li-poly") || tech.contains("lithium") -> {
            MaterialTheme.colorScheme.primary
        }

        // Tecnologías antiguas o específicas (Naranja/Terciario)
        tech.contains("ni-mh") || tech.contains("ni-cd") || tech.contains("fe") -> {
            MaterialTheme.colorScheme.tertiary
        }

        // Desconocido o No soportado (Neutro)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}