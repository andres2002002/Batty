package com.habitiora.batty.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Stable
enum class InfoRowEmphasis {
    /** Label normal + value normal. Uso general. */
    Default,
    /** Label atenuado + value destacado. Uso en datos eléctricos. */
    ValueHighlighted,
    /** Ambos atenuados. Uso para datos no disponibles (-1). */
    Muted,
}

/**
 * Fila label → value alineados en extremos opuestos.
 * Diseñada para integrarse en contenedores (como BattyCard) que manejan el ritmo vertical mediante Arrangement.
 *
 * @param label Texto descriptivo izquierdo.
 * @param value Texto del dato derecho. Si es null renderiza "—".
 * @param modifier Modificador aplicado al Row.
 * @param emphasis Controla pesos visuales. Ver [InfoRowEmphasis].
 * @param valueColor Override de color para el value. Útil para colores funcionales (e.g. error).
 */
@Composable
fun InfoRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    emphasis: InfoRowEmphasis = InfoRowEmphasis.Default,
    valueColor: Color = Color.Unspecified,
) {
    val labelStyle = when (emphasis) {
        InfoRowEmphasis.Muted -> MaterialTheme.typography.bodySmall
        else -> MaterialTheme.typography.bodyMedium
    }

    val labelColor = when (emphasis) {
        InfoRowEmphasis.Default -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val valueStyle = when (emphasis) {
        InfoRowEmphasis.Default -> MaterialTheme.typography.bodyMedium
        // titleSmall en M3 es 14sp con peso Medium, ideal para reemplazar el .copy(fontWeight)
        InfoRowEmphasis.ValueHighlighted -> MaterialTheme.typography.titleSmall
        InfoRowEmphasis.Muted -> MaterialTheme.typography.bodySmall
    }

    val resolvedValueColor = if (valueColor != Color.Unspecified) {
        valueColor
    } else {
        when (emphasis) {
            InfoRowEmphasis.Muted -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> MaterialTheme.colorScheme.onSurface
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = labelStyle,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value ?: "—",
            style = valueStyle,
            color = resolvedValueColor,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}