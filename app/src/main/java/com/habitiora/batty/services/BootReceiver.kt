package com.habitiora.batty.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habitiora.batty.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var monitorSettingsRepository: SettingsRepository

    /**
     * goAsync() extiende el timeout del receiver de 5s a ~30s para hacer
     * la lectura de DataStore sin bloquear el main thread.
     */
    override fun onReceive(context: Context, intent: Intent) {
        val validActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON",
        )
        if (intent.action !in validActions) return

        Timber.d("BootReceiver → ${intent.action}")

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val settings = monitorSettingsRepository.get()
                when {
                    !settings.startOnBoot -> {
                        Timber.d("startOnBoot=false — skipping auto-start")
                    }
                    !settings.monitorBattery -> {
                        // startOnBoot activo pero el usuario desactivó el monitor —
                        // no tiene sentido arrancar el servicio
                        Timber.d("startOnBoot=true but monitorBattery=false — skipping")
                    }
                    else -> {
                        Timber.i("Boot auto-start → launching BatteryMonitorService")
                        context.startForegroundService(BatteryMonitorService.startIntent(context))
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "BootReceiver failed to read settings")
            } finally {
                pendingResult.finish()
            }
        }
    }
}