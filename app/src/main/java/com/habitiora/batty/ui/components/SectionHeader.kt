package com.habitiora.batty.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Label de sección estándar basado en M3.
 * Uso: Settings screen headers, agrupaciones dentro de cards.
 *
 * @param title Texto del header.
 * @param modifier Modifier externo — el padding vertical interno lo maneja el componente
 * para mantener el ritmo del "espaciado denso" (8.dp).
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(bottom = 8.dp),
    )
}