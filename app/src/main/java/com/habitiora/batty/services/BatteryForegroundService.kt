package com.habitiora.batty.services;

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.habitiora.batty.R
import com.habitiora.batty.data.proto.ThresholdsDataStore
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BatteryForegroundService : Service() {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var batteryReceiver: BroadcastReceiver
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var thresholdsDataStore: ThresholdsDataStore

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createForegroundChannel()
        notificationHelper.createCriticalChannel()
        // Arrancamos en foreground con un texto inicial
        startForeground(NotificationHelper.BATTERY_MONITOR_NOTIFICATION_ID, buildNotification(0, false, "--"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // Extraemos estado
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                // Actualizamos la notificación
                updateNotification(level, isCharging, "${level}%")

                checkForNotifications(level)
            }
        }
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /** Construye la notificación mostrando nivel y estado */
    private fun buildNotification(level: Int, isCharging: Boolean, contentText: String): Notification {
        val chargingText = if (isCharging) "Cargando" else "Descargando"
        return NotificationCompat.Builder(this, NotificationHelper.BATTERY_MONITOR_CHANNEL_ID)
            .setContentTitle("Batería: $level%")
            .setContentText("$chargingText — $contentText")
            .setSmallIcon(R.drawable.rounded_battery_horiz_050_24)
            .setOnlyAlertOnce(true)      // No sonar cada vez que actualiza
            .setOngoing(true)            // Para que sea persistente
            .build()
    }

    /** Actualiza la notificación del foreground service */
    private fun updateNotification(level: Int, isCharging: Boolean, contentText: String) {
        val notification = buildNotification(level, isCharging, contentText)
        // Opción A: Volver a arrancar foreground con la misma ID
        startForeground(NotificationHelper.BATTERY_MONITOR_NOTIFICATION_ID, notification)
        // Opción B: Usar NotificationManager
        // NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    private fun checkForNotifications(level: Int) {
        coroutineScope.launch {
            val lowThresholds = thresholdsDataStore.lowThresholds.first().sorted()
            val highThresholds = thresholdsDataStore.highThresholds.first().sorted()
            val triggeredLow = thresholdsDataStore.triggeredLow.first()
            val triggeredHigh = thresholdsDataStore.triggeredHigh.first()

            if (level < 50) {
                if (lowThresholds.isEmpty()) return@launch
                var lowThreshold = lowThresholds.first()
                lowThresholds.forEachIndexed { index, value ->
                    if (level <= value) {
                        lowThreshold = value
                        return@forEachIndexed
                    }
                }
                if (level <= lowThreshold && triggeredLow[lowThreshold] != true) {
                    notificationHelper.showCriticalNotification(
                        "Batería baja",
                        "Nivel al $level%"
                    )
                    thresholdsDataStore.markLowTriggered(lowThreshold, true)
                }
                if (level > lowThreshold && triggeredLow[lowThreshold] == true) {
                    thresholdsDataStore.markLowTriggered(lowThreshold, false)
                }
            }
            else if (level > 50) {
                if (highThresholds.isEmpty()) return@launch
                var highThreshold = highThresholds.first()
                highThresholds.forEachIndexed { index, value ->
                    if (level >= value) {
                        highThreshold = value
                        return@forEachIndexed
                    }
                }
                if (level >= highThreshold && triggeredHigh[highThreshold] != true) {
                    notificationHelper.showCriticalNotification(
                        "Batería alta",
                        "Nivel al $level%"
                    )
                    thresholdsDataStore.markHighTriggered(highThreshold, true)
                }
                if (level < highThreshold && triggeredHigh[highThreshold] == true) {
                    thresholdsDataStore.markHighTriggered(highThreshold, false)
                }
            }
        }
    }
}
