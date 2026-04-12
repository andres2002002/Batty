package com.habitiora.batty.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.habitiora.batty.data.manager.ThresholdsManager
import com.habitiora.batty.data.source.BatteryInfoSource
import com.habitiora.batty.domain.model.BatteryInfo
import com.habitiora.batty.domain.repository.SettingsRepository
import com.habitiora.batty.domain.useCase.SaveBatterySnapshotUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BatteryMonitorService : Service() {

    @Inject
    lateinit var saveBatterySnapshotUseCase: SaveBatterySnapshotUseCase
    @Inject
    lateinit var notificationHelper: NotificationHelper
    @Inject
    lateinit var batteryInfoSource: BatteryInfoSource
    @Inject
    lateinit var thresholdsManager: ThresholdsManager
    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var batteryReceiver: BroadcastReceiver? = null

    @Volatile
    private var currentInfo: BatteryInfo = BatteryInfo.empty()

    // Throttling — evita flood de actualizaciones de notificación
    private var lastNotifiedLevel: Int = -1
    private var lastNotifiedIsCharging: Boolean? = null

    companion object {
        const val ACTION_START = "battery.monitor.ACTION_START"
        const val ACTION_STOP = "battery.monitor.ACTION_STOP"

        private const val SAVE_INTERVAL_MS = 5 * 60 * 1000L
        private const val NOTIFICATION_THROTTLE_MS = 3_000L

        fun startIntent(context: Context): Intent =
            Intent(context, BatteryMonitorService::class.java).apply { action = ACTION_START }

        fun stopIntent(context: Context): Intent =
            Intent(context, BatteryMonitorService::class.java).apply { action = ACTION_STOP }
    }

    override fun onCreate() {
        super.onCreate()
        currentInfo = batteryInfoSource.getCurrent()
        registerBatteryReceiver()
        observeMonitorSetting()
        Timber.d("BatteryMonitorService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                serviceScope.launch {
                    val settings = settingsRepository.get()
                    if (settings.monitorBattery) {
                        startForeground()
                    } else {
                        // Arrancó pero el setting está desactivado — no iniciar
                        Timber.d("monitorBattery=false on start, stopping self")
                        stopSelf()
                    }
                }
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    private fun observeMonitorSetting() {
        serviceScope.launch {
            settingsRepository.observe()
                .map { it.monitorBattery }
                .distinctUntilChanged()
                .drop(1) // ignorar el valor inicial — onStartCommand ya lo maneja
                .collect { monitorBattery ->
                    if (!monitorBattery) {
                        Timber.i("monitorBattery disabled — stopping foreground service")
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                    // Si cambia a true: el usuario debería mandar ACTION_START desde la UI,
                    // no relanzamos desde aquí para no crear servicios fantasma.
                }
        }
    }

    private fun startForeground() {
        val notification = notificationHelper.buildForegroundNotification(
            info = currentInfo,
            thresholdState = thresholdsManager.getCurrentState()
        )
        ServiceCompat.startForeground(
            this,
            NotificationHelper.FOREGROUND_NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else 0
        )
        startPeriodicSave()
        Timber.d("Foreground service started")
    }

    private fun startPeriodicSave() {
        serviceScope.launch {
            while (isActive) {
                runCatching { saveBatterySnapshotUseCase(currentInfo) }
                    .onFailure { Timber.e(it, "Error persisting snapshot") }
                delay(SAVE_INTERVAL_MS)
            }
        }
    }

    private fun registerBatteryReceiver() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action != Intent.ACTION_BATTERY_CHANGED) return
                val info = batteryInfoSource.fromIntent(intent)
                currentInfo = info
                onBatteryInfoChanged(info)
            }
        }
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun onBatteryInfoChanged(info: BatteryInfo) {
        serviceScope.launch {
            // Threshold check — siempre, independiente del throttle
            runCatching {
                thresholdsManager.evaluate(
                    level = info.level,
                    isCharging = info.isCharging
                )
            }
                .onSuccess { event -> event?.let { notificationHelper.showThresholdAlert(it) } }
                .onFailure { Timber.e(it, "Error evaluating thresholds") }

            // Throttle para la notificación persistente
            if (shouldUpdateForeground(info)) {
                updateForegroundNotification(info)
                lastNotifiedLevel = info.level
                lastNotifiedIsCharging = info.isCharging
            }
        }
    }

    /**
     * Actualiza la notificación solo cuando hay un cambio real:
     * - Primer update
     * - Cambio de ±1% en nivel
     * - Cambio en estado de carga
     */
    private fun shouldUpdateForeground(info: BatteryInfo): Boolean = when {
        lastNotifiedLevel == -1 -> true
        lastNotifiedIsCharging != info.isCharging -> true
        kotlin.math.abs(info.level - lastNotifiedLevel) >= 1 -> true
        else -> false
    }

    private fun updateForegroundNotification(info: BatteryInfo) {
        val notification = notificationHelper.buildForegroundNotification(
            info = info,
            thresholdState = thresholdsManager.getCurrentState()
        )
        notificationHelper.notify(NotificationHelper.FOREGROUND_NOTIFICATION_ID, notification)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        serviceScope.launch {
            val settings = settingsRepository.get()
            if (settings.monitorBattery) {
                Timber.d("Task removed with monitorBattery=true — restarting")
                startForegroundService(startIntent(this@BatteryMonitorService))
            }
        }
    }

    override fun onDestroy() {
        batteryReceiver?.let { runCatching { unregisterReceiver(it) } }
        serviceScope.cancel()
        Timber.d("BatteryMonitorService destroyed")
        super.onDestroy()
    }

    inner class LocalBinder : Binder() {
        fun getService(): BatteryMonitorService = this@BatteryMonitorService
    }

    private val binder = LocalBinder()

    // Reemplazar: override fun onBind(intent: Intent?): IBinder? = null
    override fun onBind(intent: Intent?): IBinder = binder
}