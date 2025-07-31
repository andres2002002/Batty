package com.habitiora.batty.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.habitiora.batty.R
import jakarta.inject.Inject
import timber.log.Timber

sealed class PermissionRequest(val code: String?){
    abstract val name: String
    @get:StringRes abstract val title: Int
    @get:StringRes abstract val description: Int
    @get:StringRes abstract val route: Int
    @SuppressLint("InlinedApi")
    data object NotificationAccess: PermissionRequest(Manifest.permission.POST_NOTIFICATIONS){
        override val name: String = "Notification Access"
        override val title: Int = R.string.message_notification_title
        override val description: Int = R.string.message_notification_description
        override val route: Int = R.string.message_notification_route
    }
    data object DndAccess : PermissionRequest(null){
        override val name: String = "DND Access"
        override val title: Int = R.string.message_dnd_title
        override val description: Int = R.string.message_dnd_description
        override val route: Int = R.string.message_dnd_route
    }
}

object PermissionsHelper {
    private val levelApi: Int = Build.VERSION.SDK_INT
    private fun isVersionTiramisuOrHigher(): Boolean = levelApi >= Build.VERSION_CODES.TIRAMISU

    private fun checkPermission(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun launchPermission(
        permission: String,
        launcher: ManagedActivityResultLauncher<String, Boolean>
    ) {
        launcher.launch(permission)
    }

    fun checkAndRequestPermission(
        context: Context,
        permission: String,
        launcher: ManagedActivityResultLauncher<String, Boolean>,
        onGranted: () -> Unit
    ) {
        val permissionState = ContextCompat.checkSelfPermission(context, permission)
        if (permissionState == PackageManager.PERMISSION_GRANTED){
            onGranted()
        }
        else {
            launcher.launch(permission)
        }
    }

    fun needs(ctx: Context, request: PermissionRequest): Boolean =
        when(request) {
            PermissionRequest.NotificationAccess -> {
                // Verificar si se necesita solicitar permiso
                // si la version es >= 33 y no se tiene permiso
                isVersionTiramisuOrHigher() && !checkPermission(ctx, request.code!!)
            }

            PermissionRequest.DndAccess -> {
                val nm = ctx.getSystemService(NotificationManager::class.java)
                !nm.isNotificationPolicyAccessGranted
            }
        }

    /** Lanza la pantalla de ajustes para DND */
    fun launchDndSettings(ctx: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        ctx.startActivity(intent)
    }
}