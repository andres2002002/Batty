package com.habitiora.batty.services.controller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.app.Service.START_NOT_STICKY
import android.app.Service.START_STICKY
import android.app.Service.STOP_FOREGROUND_REMOVE
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.habitiora.batty.MainActivity
import com.habitiora.batty.R
import com.habitiora.batty.data.manager.BatterySaveManager
import com.habitiora.batty.data.manager.RegisterReceiverManager
import com.habitiora.batty.data.manager.ThresholdsManager
import com.habitiora.batty.domain.model.BatteryState
import com.habitiora.batty.domain.repository.SettingsRepository
import com.habitiora.batty.services.BatteryForegroundService
import com.habitiora.batty.services.BatteryReceiver
import com.habitiora.batty.services.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PARA IMPLEMENTAR DESPUES
 * Controller/HOF que encapsula toda la lógica de:
 * - Registro y recuperación de BroadcastReceiver
 * - Manejo de flujos de BatteryState
 * - Throttling, notificaciones y thresholds
 * - Interacción con repositorios y managers de negocio
 */
@Singleton
class BatteryServiceController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val thresholdsManager: ThresholdsManager,
    private val batteryReceiver: BatteryReceiver,
    private val receiverManager: RegisterReceiverManager,
    private val notificationHelper: NotificationHelper,
    private val batterySaveManager: BatterySaveManager,
    private val settingsRepository: SettingsRepository
) {

    companion object {
        const val ACTION_START_MONITORING = "com.habitiora.batty.START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.habitiora.batty.STOP_MONITORING"
        const val EXTRA_FORCE_RESTART = "force_restart"

        private const val COMPONENT_ID = "BatteryService"

        private const val BATTERY_UPDATE_THROTTLE_MS = 5000L // 5 segundos
        private const val INVALID_BATTERY_LEVEL = -1
    }

    // Cache para evitar actualizaciones innecesarias
    private var lastBatteryLevel = INVALID_BATTERY_LEVEL
    private var lastChargingState: Boolean? = null
    private var lastUpdateTime = 0L

    lateinit var service: BatteryForegroundService
    lateinit var scope: CoroutineScope

    private fun setForegroundService(service: BatteryForegroundService) {
        this.service = service
    }

    fun setCoroutineScope(coroutineScope: CoroutineScope) {
        this.scope = coroutineScope
    }

    fun initialize(service: BatteryForegroundService, coroutineScope: CoroutineScope) {
        setForegroundService(service)
        setCoroutineScope(coroutineScope)
        Timber.i("BatteryForegroundService creado")

        // Crear canales de notificación
        notificationHelper.createForegroundChannel()
        notificationHelper.createCriticalChannel()

        startMonitoring()
        setupBatteryReceiver()
    }

    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("onStartCommand: ${intent?.action}")

        when (intent?.action) {
            ACTION_START_MONITORING -> startMonitoring()
            ACTION_STOP_MONITORING -> {
                // Quitamos la notificación y detenemos el servicio
                service.stopForeground(STOP_FOREGROUND_REMOVE)
                service.stopSelf()

                return START_NOT_STICKY
            }
            else -> startMonitoring() // Comportamiento por defecto
        }

        return START_STICKY
    }

    private fun startMonitoring() {
        // Iniciar monitoreo con el nuevo métod
        val registered = receiverManager.startMonitoring(service, COMPONENT_ID)

        if (registered) {

            // Iniciar foreground service
            val initialNotification = buildForegroundNotification(
                level = 0,
                isCharging = false,
                contentText = context.getString(R.string.initializing_monitoring)
            )
            service.startForeground(NotificationHelper.BATTERY_MONITOR_NOTIFICATION_ID, initialNotification)

            // Obtener estado inicial de batería
            requestInitialBatteryState()
        } else {
            Timber.e("No se pudo registrar el BatteryReceiver")
            service.stopSelf()
        }
    }

    private fun setupBatteryReceiver() {
        scope.launch {
            batteryReceiver.batteryState.collect { batteryState ->
                batteryState?.let {
                    handleBatteryChanged(it)
                } ?: run {
                    // Si perdemos el estado, intentar recuperar el registro
                    if (!receiverManager.verifyAndRecoverRegistration(service, COMPONENT_ID)) {
                        Timber.e("No se pudo recuperar el registro del BatteryReceiver")
                    }
                }
            }
        }
    }

    private fun handleBatteryChanged(state: BatteryState) {
        try {
            val level = state.batteryLevel
            val isCharging = state.isCharging

            if (!isValidBatteryState(state)) {
                Timber.w("Estado de batería inválido: $state")
                return
            }

            // Throttling para evitar demasiadas actualizaciones
            val currentTime = System.currentTimeMillis()
            val shouldUpdate = shouldUpdateNotification(level, isCharging, currentTime)

            if (shouldUpdate) {
                updateForegroundNotification(level, isCharging)
                checkBatteryThresholds(level, isCharging)
                updateCache(level, isCharging, currentTime)
            }

            scope.launch {
                saveBatteryHistory(state)
            }

            logBatteryMetrics(state)

        } catch (e: SecurityException) {
            Timber.e(e, "Error de permisos en batería")
            // Manejar específicamente errores de permisos
        } catch (e: IllegalStateException) {
            Timber.e(e, "Estado inválido del servicio")
            // Reiniciar servicio si es necesario
        } catch (e: Exception) {
            Timber.e(e, "Error procesando cambio de batería")
        }
    }

    private suspend fun saveBatteryHistory(state: BatteryState) {
        try {
            batterySaveManager.saveBatteryState(state)

        } catch (e: Exception) {
            Timber.e(e, "Error guardando historial de batería")
        }
    }

    private fun logBatteryMetrics(state: BatteryState) {
        Timber.d("Batería - Nivel: ${state.batteryLevel}%, Cargando: ${state.isCharging}, Temp: ${state.temperature/10}°C, Voltaje: ${state.voltage}mV")
    }

    private fun isValidBatteryState(state: BatteryState): Boolean {
        return state.batteryLevel in 0..100 &&
                state.temperature > -500 && // Temperatura razonable
                state.voltage > 0
    }

    private fun shouldUpdateNotification(level: Int, isCharging: Boolean, currentTime: Long): Boolean {
        return when {
            // Primera actualización
            lastBatteryLevel == INVALID_BATTERY_LEVEL -> true
            // Cambio en el estado de carga
            lastChargingState != isCharging -> true
            // Cambio significativo en el nivel (±1%)
            kotlin.math.abs(level - lastBatteryLevel) >= 1 -> true
            // Throttling temporal
            (currentTime - lastUpdateTime) >= BATTERY_UPDATE_THROTTLE_MS -> true
            else -> false
        }
    }

    private fun updateCache(level: Int, isCharging: Boolean, currentTime: Long) {
        lastBatteryLevel = level
        lastChargingState = isCharging
        lastUpdateTime = currentTime
    }

    private fun checkBatteryThresholds(level: Int, isCharging: Boolean) {
        scope.launch {
            try {
                val batteryIcon = getBatteryIcon(level, isCharging)
                val notificationEnabled = settingsRepository.isBatteryNotificationEnabled.first()
                val isDndActive = settingsRepository.isNotificationsDNDEnabled.first()
                thresholdsManager.checkForNotifications(level, isCharging) { title, message ->
                    if (notificationEnabled) displayBatteryNotification(title, message, batteryIcon, isDndActive)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error verificando umbrales de batería")
            }
        }
    }

    private fun displayBatteryNotification(title: String, message: String, icon: Int, isCritical: Boolean) {
        if (hasNotificationPermission()) {
            notificationHelper.displayBatteryNotification(title, message, icon, isCritical)
            Timber.i("Notificación crítica mostrada: $title - $message")
        } else {
            Timber.w("Sin permisos para mostrar notificaciones")
        }
    }

    private fun requestInitialBatteryState() {
        // Solicitar estado inicial de batería
        val batteryIntent = service.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        batteryIntent?.let { intent ->
            handleBatteryChanged(batteryReceiver.createBatteryState(service, intent, System.currentTimeMillis()))
        }
    }

    private fun buildForegroundNotification(level: Int, isCharging: Boolean, contentText: String): Notification {
        val chargingId = if (isCharging) R.string.status_charging else R.string.status_discharging
        val chargingText = context.getString(chargingId)
        val batteryIcon = getBatteryIcon(level, isCharging)

        // Intent para abrir la app al tocar la notificación
        val openAppIntent = Intent(service, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            service, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentTitle = context.getString(R.string.battery_state) + " $level%"

        return NotificationCompat.Builder(service, NotificationHelper.BATTERY_MONITOR_CHANNEL_ID)
            .setContentTitle(contentTitle)
            .setContentText("$chargingText — $contentText")
            .setSmallIcon(batteryIcon)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setShowWhen(false)
            .setLocalOnly(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun getBatteryIcon(level: Int, isCharging: Boolean): Int {
        return when {
            isCharging -> when {
                level < 25 -> R.drawable.batty_icon_close
                level < 50 -> R.drawable.batty_icon_semi_close
                level < 75 -> R.drawable.batty_icon_semi_open
                else -> R.drawable.batty_icon_open
            }
            else -> when {
                level <= 10 -> R.drawable.batty_icon_close
                level < 25 -> R.drawable.batty_icon_close
                level < 50 -> R.drawable.batty_icon_semi_close
                level < 75 -> R.drawable.batty_icon_semi_open
                else -> R.drawable.batty_icon_open
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateForegroundNotification(level: Int, isCharging: Boolean) {
        if (!hasNotificationPermission()) {
            Timber.w("Sin permisos para actualizar notificación")
            return
        }

        val contentText = generateContentText(level, isCharging)
        val notification = buildForegroundNotification(level, isCharging, contentText)

        scope.launch {
            try {
                val monitoringEnabled = settingsRepository.isBatteryMonitorEnabled.first()
                if (monitoringEnabled) {
                    NotificationManagerCompat.from(service)
                        .notify(NotificationHelper.BATTERY_MONITOR_NOTIFICATION_ID, notification)
                } else{
                    service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
                    service.stopSelf()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error actualizando notificación de foreground")
            }
        }
    }

    private fun generateContentText(level: Int, isCharging: Boolean): String {
        val triggerState = thresholdsManager.getCurrentState()

        return when {
            isCharging ->context.getString(R.string.charging_until_completing)
            triggerState.hasActiveTrigger -> {
                val triggerType = if (triggerState.isHighTriggerActive) R.string.on_high_battery_threshold
                else R.string.on_low_battery_threshold
                context.getString(triggerType)
            }
            else -> context.getString(R.string.monitoring_level)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(service, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}