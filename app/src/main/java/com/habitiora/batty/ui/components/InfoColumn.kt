package com.habitiora.batty.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

object InfoColumnDefaults {
    object IconSize {
        val ExtraSmall = 12.dp
        val Small = 18.dp
        val Medium = 24.dp
        val Large = 36.dp
        val ExtraLarge = 48.dp
    }
}
@Composable
fun InfoColumn(
    label: String?,
    value: String?,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    emphasis: InfoRowEmphasis = InfoRowEmphasis.Default,
    valueColor: Color = Color.Unspecified,
){
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

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon()
        Spacer(modifier = Modifier.padding(4.dp))
        label?.let {
            Text(
                text = label,
                style = labelStyle,
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.padding(4.dp))
        }
        Text(
            text = value ?: "—",
            style = valueStyle,
            color = resolvedValueColor,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}