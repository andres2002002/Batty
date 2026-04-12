package com.habitiora.batty.ui.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
enum class BattyCardVariant {
    Elevated, // Variante por defecto según lineamientos de UI/UX
    Default,
    Outlined,
    Filled,
}

@Stable
object BattyCardDefaults {
    val Shape: Shape @Composable get() = MaterialTheme.shapes.large

    // Espaciados según la regla: denso interno, aireado externo
    val SoftPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    val ContentPadding = PaddingValues(all = 20.dp) // Más aire interno
    val HeaderPadding = PaddingValues(top = 20.dp, bottom = 12.dp, start = 20.dp, end = 20.dp)
    // Espaciado entre elementos principales del Dashboard
    val InterGroupSpacing = 16.dp
    // Espaciado denso interno para datos
    val InnerSpacing = 8.dp
    // Tokens de elevación de M3
    val DefaultTonalElevation: Dp = 0.dp
    val ElevatedTonalElevation: Dp = 1.dp // M3 ElevatedCard default (Level 1)

    // Borde Outlined
    val OutlinedBorderWidth: Dp = 1.dp

    @Composable
    fun defaultColors(): CardColors = CardDefaults.cardColors()

    @Composable
    fun elevatedColors(): CardColors = CardDefaults.elevatedCardColors()

    @Composable
    fun outlinedColors(): CardColors = CardDefaults.outlinedCardColors()

    @Composable
    fun filledColors(): CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    @Composable
    fun defaultElevation(): CardElevation = CardDefaults.cardElevation(
        defaultElevation = DefaultTonalElevation,
    )

    @Composable
    fun elevatedElevation(): CardElevation = CardDefaults.elevatedCardElevation(
        defaultElevation = ElevatedTonalElevation,
    )

    @Composable
    fun outlinedBorder(color: Color = MaterialTheme.colorScheme.outlineVariant): BorderStroke =
        BorderStroke(width = OutlinedBorderWidth, color = color)
}

/**
 * Componente base de tarjeta para agrupar información.
 *
 * @param modifier Modificador aplicado a la raíz.
 * @param variant Controla el tratamiento de superficie. Por defecto [BattyCardVariant.Elevated].
 * @param header Slot opcional renderizado sobre [content] con [BattyCardDefaults.HeaderPadding].
 * @param contentPadding Padding aplicado al slot [content].
 * @param onClick Si no es nulo, la tarjeta utiliza la sobrecarga interactiva de M3.
 * @param enabled Controla el estado del clic.
 * @param colors Sobrescribe los colores por defecto resueltos por [variant].
 * @param elevation Sobrescribe la elevación por defecto resuelta por [variant].
 * @param content Cuerpo principal de la tarjeta. Internamente usa un Arrangement denso.
 */
@Composable
fun BattyCard(
    modifier: Modifier = Modifier,
    variant: BattyCardVariant = BattyCardVariant.Elevated,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    contentPadding: PaddingValues = BattyCardDefaults.ContentPadding,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: CardColors? = null,
    elevation: CardElevation? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val resolvedColors = colors ?: when (variant) {
        BattyCardVariant.Elevated -> BattyCardDefaults.elevatedColors()
        BattyCardVariant.Default -> BattyCardDefaults.defaultColors()
        BattyCardVariant.Outlined -> BattyCardDefaults.outlinedColors()
        BattyCardVariant.Filled -> BattyCardDefaults.filledColors()
    }

    val resolvedElevation = elevation ?: when (variant) {
        BattyCardVariant.Elevated -> BattyCardDefaults.elevatedElevation()
        else -> BattyCardDefaults.defaultElevation()
    }

    val shape = BattyCardDefaults.Shape
    val baseModifier = modifier.fillMaxWidth()

    when (variant) {
        BattyCardVariant.Elevated -> {
            if (onClick != null) {
                ElevatedCard(
                    onClick = onClick,
                    modifier = baseModifier,
                    enabled = enabled,
                    shape = shape,
                    colors = resolvedColors,
                    elevation = resolvedElevation
                ) {
                    BattyCardInternalContent(header, contentPadding, content)
                }
            } else {
                ElevatedCard(
                    modifier = baseModifier,
                    shape = shape,
                    colors = resolvedColors,
                    elevation = resolvedElevation
                ) {
                    BattyCardInternalContent(header, contentPadding, content)
                }
            }
        }

        BattyCardVariant.Outlined -> {
            val border = BattyCardDefaults.outlinedBorder()
            if (onClick != null) {
                OutlinedCard(
                    onClick = onClick,
                    modifier = baseModifier,
                    enabled = enabled,
                    shape = shape,
                    colors = resolvedColors,
                    elevation = resolvedElevation,
                    border = border
                ) {
                    BattyCardInternalContent(header, contentPadding, content)
                }
            } else {
                OutlinedCard(
                    modifier = baseModifier,
                    shape = shape,
                    colors = resolvedColors,
                    elevation = resolvedElevation,
                    border = border
                ) {
                    BattyCardInternalContent(header, contentPadding, content)
                }
            }
        }

        BattyCardVariant.Default,
        BattyCardVariant.Filled -> {
            if (onClick != null) {
                Card(
                    onClick = onClick,
                    modifier = baseModifier,
                    enabled = enabled,
                    shape = shape,
                    colors = resolvedColors,
                    elevation = resolvedElevation
                ) {
                    BattyCardInternalContent(header, contentPadding, content)
                }
            } else {
                Card(
                    modifier = baseModifier,
                    shape = shape,
                    colors = resolvedColors,
                    elevation = resolvedElevation
                ) {
                    BattyCardInternalContent(header, contentPadding, content)
                }
            }
        }
    }
}

@Composable
private fun BattyCardInternalContent(
    header: (@Composable ColumnScope.() -> Unit)?,
    contentPadding: PaddingValues,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (header != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BattyCardDefaults.HeaderPadding),
            verticalArrangement = Arrangement.spacedBy(BattyCardDefaults.InnerSpacing)
        ) {
            header()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(BattyCardDefaults.InnerSpacing)
    ) {
        content()
    }
}