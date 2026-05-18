package com.habitiora.batty.ui.screens.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitiora.batty.BuildConfig
import com.habitiora.batty.R

@Composable
fun InfoScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // Reducimos el espaciado global porque las secciones ahora se delimitan por diseño
        ) {
            item {
                AppHeader()
            }

            item {
                InfoSection(
                    title = stringResource(R.string.app_description_long_title),
                    content = stringResource(R.string.app_description_long)
                )
            }

            item {
                InfoSection(
                    title = stringResource(R.string.main_functions),
                    content = ""
                ) {
                    InfoFeature(
                        icon = ImageVector.vectorResource(R.drawable.baseline_visibility_24),
                        title = stringResource(R.string.background_monitoring),
                        description = stringResource(R.string.background_monitoring_description)
                    )
                    InfoFeature(
                        icon = ImageVector.vectorResource(R.drawable.baseline_notifications_active_24),
                        title = stringResource(R.string.notifications_criticals),
                        description = stringResource(R.string.notifications_criticals_description)
                    )
                    InfoFeature(
                        icon = ImageVector.vectorResource(R.drawable.baseline_history_24),
                        title = stringResource(R.string.history_monitoring),
                        description = stringResource(R.string.history_monitoring_description)
                    )
                }
            }

            item {
                InfoSection(
                    title = stringResource(R.string.permissions_required),
                    content = ""
                ) {
                    InfoPermission(
                        title = stringResource(R.string.notifications_permission),
                        description = stringResource(R.string.notifications_permission_description)
                    )
                    InfoPermission(
                        title = stringResource(R.string.dnd_permission),
                        description = stringResource(R.string.dnd_permission_description)
                    )
                }
            }

            item {
                InfoSection(
                    title = stringResource(R.string.privacity_and_data_title),
                    content = stringResource(R.string.privacity_and_data_description)
                )
            }

            item {
                InfoSection(
                    title = stringResource(R.string.info_settings_title),
                    content = stringResource(R.string.info_settings_description)
                )
            }

            item {
                AppFooter()
            }
        }
    }
}

@Composable
private fun AppHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
    ) {
        // Envolvemos el logo en un contenedor suave para darle jerarquía
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.batty_icon_fg),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(150.dp),
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = stringResource(R.string.app_description_short),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun AppFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.info_app_version_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${stringResource(R.string.info_app_version)}: ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${stringResource(R.string.info_last_update)}: Julio 2025",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun InfoSection(
    title: String,
    content: String,
    additionalContent: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (content.isNotEmpty()) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )
        }

        if (additionalContent != null) {
            Spacer(modifier = Modifier.height(if (content.isNotEmpty()) 16.dp else 4.dp))
            additionalContent()
        }
    }

    // Divisor sutil entre secciones
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    )
}

@Composable
fun InfoFeature(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun InfoPermission(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.baseline_security_24),
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp), // Alineación óptica con el texto
            tint = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}