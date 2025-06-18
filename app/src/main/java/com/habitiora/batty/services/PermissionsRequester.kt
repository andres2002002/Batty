package com.habitiora.batty.services

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import jakarta.inject.Inject

interface PermissionsRequesterFactory {
    fun create(activity: Activity): PermissionsRequester
}

class PermissionsRequesterImpl @Inject constructor() : PermissionsRequesterFactory {
    override fun create(activity: Activity): PermissionsRequester =
        PermissionsRequester(activity)
}

class PermissionsRequester @Inject constructor(
    private val activity: Activity
) {
    /**
     * Solicita el acceso al modo DND en el dispositivo.
     */
    fun requestDndAccess() {
        val nm = activity.getSystemService(NotificationManager::class.java)
        if (!nm.isNotificationPolicyAccessGranted) {
            // Abre la pantalla de Ajustes para que el usuario otorgue el permiso
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            activity.startActivity(intent)
        }
    }

    /**
     * Solicita el acceso a las notificaciones en el dispositivo.
     */
    fun  requestNotificationAccess() {
       val checkVersion = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
       if(checkVersion){
           val checkPermission = ContextCompat.checkSelfPermission(
               activity,
               Manifest.permission.POST_NOTIFICATIONS
           ) != PackageManager.PERMISSION_GRANTED
           if(checkPermission){
               ActivityCompat.requestPermissions(
                   activity,
                   arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                   1001
               )
           }
       }
   }

    /**
     * Solicita el acceso a las optimizaciones de batería en el dispositivo.
     */
    fun requestIgnoreBatteryOptimizations() {
        val pm = activity.getSystemService(PowerManager::class.java)
        val packageName = activity.packageName
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = "package:$packageName".toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            activity.startActivity(intent)
        }
    }
}