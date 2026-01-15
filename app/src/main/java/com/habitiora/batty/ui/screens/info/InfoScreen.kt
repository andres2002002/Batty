package com.habitiora.batty.ui.screens.info

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitiora.batty.BuildConfig
import com.habitiora.batty.R

@Composable
fun InfoScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Header con logo/icono de la app
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.batty_logo_foreground),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.size(80.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.app_description_short),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                        icon = ImageVector.vectorResource(R.drawable.baseline_visibility_24), //Visibility,
                        title = stringResource(R.string.background_monitoring),
                        description = stringResource(R.string.background_monitoring_description)
                    )
                    InfoFeature(
                        icon = ImageVector.vectorResource(R.drawable.baseline_notifications_active_24), //NotificationsActive,
                        title = stringResource(R.string.notifications_criticals),
                        description = stringResource(R.string.notifications_criticals_description)
                    )
                    InfoFeature(
                        icon = ImageVector.vectorResource(R.drawable.baseline_history_24), //History,
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
                        description = stringResource(R.string.notifications_permission)
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
                // Footer con información de versión
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.info_app_version_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val appVersion = stringResource(R.string.info_app_version) +
                                ": " + BuildConfig.VERSION_NAME
                        Text(
                            text = appVersion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val lastUpdate = stringResource(R.string.info_last_update) +
                                ": " + "Julio 2025"
                        Text(
                            text = lastUpdate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            if (content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
            additionalContent?.invoke()
        }
    }
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
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
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
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.baseline_security_24), //Security,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}