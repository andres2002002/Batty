package com.habitiora.batty.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.habitiora.batty.R
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import timber.log.Timber
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager
){
    companion object {
        const val BATTERY_MONITOR_CHANNEL_ID = "battery_monitor_channel"
        const val BATTERY_MONITOR_NOTIFICATION_ID = 101
        const val CRITICAL_CHANNEL_ID = "critical_battery_channel"
        const val CRITICAL_NOTIFICATION_ID = 999
    }

    fun createForegroundChannel() {
        val channel = NotificationChannel(
            BATTERY_MONITOR_CHANNEL_ID,
            context.getString(R.string.battery_monitor_channel),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description =  context.getString(R.string.battery_monitor_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun createCriticalChannel() {
        val channel = NotificationChannel(
            CRITICAL_CHANNEL_ID,
            context.getString(R.string.battery_status_notification_channel),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.battery_status_notification_description)
            setBypassDnd(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {// android 13
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED){
                Timber.i("Notification permission not granted")
                return false
            }
        }
        return true
    }

    fun displayBatteryNotification(title: String, msg: String, icon: Int, isCritical: Boolean) {
        val priority = if (isCritical) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT
        val notification = NotificationCompat.Builder(context, CRITICAL_CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(msg)
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_ALARM) // sugiere alto nivel
            .build()
        Timber.i("Critical notification sent: $title - $msg")
        if (!checkPermission()) return
        NotificationManagerCompat.from(context).notify(CRITICAL_NOTIFICATION_ID, notification)
    }
}
