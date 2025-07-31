package com.habitiora.batty.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.habitiora.batty.services.PermissionRequest
import com.habitiora.batty.services.PermissionsHelper
import timber.log.Timber

@Composable
fun RequestMultiplePermissions(
    requests: List<PermissionRequest>,
    onGranted: () -> Unit = {},
    onDenied: (PermissionRequest) -> Unit = {}
) {
    require(requests.isNotEmpty()) { "Permission requests list cannot be empty" }
    var openDialog by remember { mutableStateOf(true) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val currentRequest = requests.getOrNull(currentIndex)

    // Función para avanzar al siguiente permiso
    val processNext = remember {
        {
            currentIndex++
            isProcessing = false
            if (currentIndex >= requests.size) {
                onGranted()
            }
            // Se procesará el siguiente en el próximo recompose
        }
    }

    val handleDenied = remember {
        { request: PermissionRequest ->
            isProcessing = false
            onDenied(request)
        }
    }

    // Launcher para permisos runtime (solo Notification/Location)
    val runtimeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val request = currentRequest
        if (granted && request != null) {
            Timber.d("Permission granted: ${request.code}")
            processNext()
        } else if (request != null) {
            Timber.d("Permission denied: ${request.code}")
            handleDenied(request)
        }
    }

    // Launcher para ajustes DND (no devuelve resultado real; chequeas tras volver)
    val dndLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val request = currentRequest
        if (request != null) {
            // Verificar si se concedió tras volver de settings
            if (!PermissionsHelper.needs(context, request)) {
                Timber.d("DND permission granted: ${request.code}")
                processNext()
            } else {
                Timber.d("DND permission denied: ${request.code}")
                handleDenied(request)
            }
        }
    }

    // Procesar el permiso actual
    LaunchedEffect(currentRequest, isProcessing) {
        val request = currentRequest

        if (request != null && !isProcessing) {
            isProcessing = true
            Timber.d("Processing permission ${currentIndex + 1}/${requests.size}: ${request.code}")

            // Verificar si ya tiene el permiso
            if (!PermissionsHelper.needs(context, request)) {
                Timber.d("Permission already granted: ${request.code}")
                processNext()
                return@LaunchedEffect
            }

            // Solicitar permiso según el tipo
            when (request) {
                is PermissionRequest.NotificationAccess -> {
                    PermissionsHelper.checkAndRequestPermission(context,request.code!!, runtimeLauncher){}
                }

                is PermissionRequest.DndAccess -> {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                    dndLauncher.launch(intent)
                }
            }
        }
    }

    // Log del progreso
    LaunchedEffect(currentIndex) {
        if (currentIndex < requests.size) {
            Timber.d("Permission chain progress: ${currentIndex}/${requests.size}")
        }
    }
}