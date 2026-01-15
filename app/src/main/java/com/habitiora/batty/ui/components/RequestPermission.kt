package com.habitiora.batty.ui.components

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.habitiora.batty.R
import com.habitiora.batty.services.PermissionRequest
import com.habitiora.batty.services.PermissionsHelper
import timber.log.Timber

@Composable
fun RequestPermission(
    request: PermissionRequest,
    onGranted: () -> Unit = {},
    onDenied: (PermissionRequest) -> Unit = {}
) {
    val context = LocalContext.current
    val displayDialog = PermissionsHelper.needs(context, request)
    var showDialog by remember { mutableStateOf(true) }

    // Función para avanzar al siguiente permiso
    val processNext = remember {
        {
            onGranted()
            // Se procesará el siguiente en el próximo recompose
        }
    }

    val handleDenied = remember {
        { request: PermissionRequest ->
            onDenied(request)
        }
    }

    // Launcher para permisos runtime (solo Notification/Location)
    val runtimeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Timber.d("Permission granted: ${request.code}")
            processNext()
        } else {
            Timber.d("Permission denied: ${request.code}")
            handleDenied(request)
        }
    }

    // Launcher para ajustes DND (no devuelve resultado real; chequeas tras volver)
    val dndLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (!PermissionsHelper.needs(context, request)) {
            Timber.d("DND permission granted: ${request.code}")
            processNext()
        } else {
            Timber.d("DND permission denied: ${request.code}")
            handleDenied(request)
        }
    }

    fun launchPermission() {
        // Verificar si ya está concedido
        if (!PermissionsHelper.needs(context, request)) {
            Timber.d("Permission already granted: ${request.code}")
            onGranted()
            return
        }

        when (request) {
            is PermissionRequest.NotificationAccess -> {
                PermissionsHelper.checkAndRequestPermission(
                    context,
                    request.code!!,
                    runtimeLauncher,
                    onGranted
                )
            }

            is PermissionRequest.DndAccess -> {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                dndLauncher.launch(intent)
            }
        }
    }

    if (!displayDialog) {
        LaunchedEffect(Unit) {
            launchPermission()
        }
    }

    if (showDialog && displayDialog) {
        PermissionRequestDialog(
            request = request,
            onGranted = {
                showDialog = false
                launchPermission()
            },
            onDenied = {
                showDialog = false
                onDenied(request)
            }
        )
    }
}

@Composable
private fun PermissionRequestDialog(
    request: PermissionRequest,
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDenied,
        icon = {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = Icons.Rounded.Info,
                contentDescription = "Info"
            )
        },
        title = {
            Text(text = stringResource(id = request.title), style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column {
                Text(
                    text = stringResource(id = request.description),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(id = request.route),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onGranted
            ) {
                Text(text = stringResource(id = R.string.message_allow))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDenied
            ) {
                Text(text = stringResource(id = R.string.message_deny))
            }
        }
    )
}

fun chainRequests(
    requests: List<PermissionRequest>,
    context: Context,
    runtimeLauncher: ManagedActivityResultLauncher<String, Boolean>,
    dndLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    onGranted: () -> Unit
){
    if (requests.isEmpty()) {
        onGranted()
        return
    }

    val request = requests.first()
    Timber.d("Requesting permission: ${request.code}")
    permissionsRequest(request, context, runtimeLauncher, dndLauncher){
        chainRequests(requests.drop(1), context, runtimeLauncher, dndLauncher, onGranted)
    }
}

private fun permissionsRequest(
    request: PermissionRequest,
    context: Context,
    runtimeLauncher: ManagedActivityResultLauncher<String, Boolean>,
    dndLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    onGranted: () -> Unit
) {
    when(request) {
        is PermissionRequest.NotificationAccess -> {
            PermissionsHelper.checkAndRequestPermission(context,request.code!!, runtimeLauncher, onGranted)
        }
        is PermissionRequest.DndAccess -> {
            if (PermissionsHelper.needs(context, request)) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                dndLauncher.launch(intent)
            } else onGranted()
        }
    }
}