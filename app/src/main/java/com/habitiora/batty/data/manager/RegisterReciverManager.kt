package com.habitiora.batty.data.manager

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.habitiora.batty.services.BatteryReceiver
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegisterReceiverManager @Inject constructor(
    private val batteryReceiver: BatteryReceiver
){
    private val receiver = batteryReceiver.getBatteryReceiver()
    // Registro de componentes activos con WeakReference para evitar memory leaks
    private val activeComponents = mutableMapOf<String, WeakReference<Context>>()
    private var registeredContext: WeakReference<Context>? = null

    /**
     * Registro seguro que permite múltiples componentes
     * @param context Contexto del componente
     * @param componentId ID único del componente (ej: "BatteryService", "MainActivity")
     * @param forceRegister Fuerza el registro aunque ya esté registrado (útil para recovery)
     */
    fun startMonitoring(
        context: Context,
        componentId: String,
        forceRegister: Boolean = false
    ): Boolean {
        return try {
            // Agregar componente a la lista activa
            activeComponents[componentId] = WeakReference(context.applicationContext)

            // Verificar si ya está registrado y es válido
            val currentContext = registeredContext?.get()
            val needsRegistration = currentContext == null || forceRegister

            if (needsRegistration) {
                // Intentar desregistrar el anterior si existe (por seguridad)
                currentContext?.let { safeUnregister(it) }

                // Registrar el nuevo
                val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                context.applicationContext.registerReceiver(receiver, filter)
                registeredContext = WeakReference(context.applicationContext)

                Timber.i("BatteryReceiver registrado por $componentId")
                true
            } else {
                Timber.d("BatteryReceiver ya está registrado, agregando $componentId a componentes activos")
                true
            }
        } catch (e: Exception) {
            Timber.e(e, "Error registrando BatteryReceiver para $componentId")
            false
        }
    }

    /**
     * Desregistro seguro por componente
     */
    fun stopMonitoring(componentId: String) {
        try {
            activeComponents.remove(componentId)

            // Solo desregistrar si no hay componentes activos válidos
            val hasActiveComponents = activeComponents.values.any { it.get() != null }

            if (!hasActiveComponents) {
                registeredContext?.get()?.let { context ->
                    safeUnregister(context)
                    registeredContext = null
                    Timber.i("BatteryReceiver desregistrado - no hay componentes activos")
                }
            } else {
                Timber.d("BatteryReceiver manteniéndose registrado - hay otros componentes activos")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error en stopMonitoring para $componentId")
        }
    }

    /**
     * Métod para que el servicio verifique y recupere el registro si es necesario
     */
    fun verifyAndRecoverRegistration(context: Context, componentId: String): Boolean {
        val currentContext = registeredContext?.get()
        return if (currentContext == null) {
            Timber.w("Registro perdido, intentando recuperar para $componentId")
            startMonitoring(context, componentId, forceRegister = true)
        } else {
            true
        }
    }

    /**
     * Desregistro seguro que no lanza excepciones
     */
    private fun safeUnregister(context: Context) {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            // El receiver ya estaba desregistrado, es normal
            Timber.d("Receiver ya estaba desregistrado")
        } catch (e: Exception) {
            Timber.w(e, "Error desregistrando receiver")
        }
    }

    /**
     * Obtiene una lista de componentes activos (para debugging)
     */
    fun getActiveComponents(): List<String> {
        return activeComponents.entries.mapNotNull { (id, ref) ->
            if (ref.get() != null) id else null
        }
    }

    /**
     * Verifica si el receiver está registrado y funcional
     */
    fun isRegistered(): Boolean {
        return registeredContext?.get() != null
    }

    // Resto de tu código existente permanece igual
    fun registerReceiver(context: Context, isReceiverRegistered: Boolean, onRegister: () -> Unit = {}) {
        // Métod legacy para compatibilidad - redirige al nuevo método
        if (startMonitoring(context, "legacy")) {
            onRegister()
        }
    }

    fun unregisterReceiver(context: Context, isReceiverRegistered: Boolean, onUnregister: () -> Unit = {}) {
        // Métod legacy para compatibilidad
        stopMonitoring("legacy")
        onUnregister()
    }
}