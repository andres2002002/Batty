package com.habitiora.batty.data.manager

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.habitiora.batty.R
import com.habitiora.batty.data.proto.ThresholdsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

class ThresholdsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val thresholdsDataStore: ThresholdsDataStore
){

    private companion object{
        const val MIN_LEVEL = 1
        const val MAX_LEVEL = 100
    }

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex() // Para evitar condiciones de carrera

    // Estados reactivos
    private val lowThresholdsState: StateFlow<List<Int>> = thresholdsDataStore.lowThresholds
        .map { thresholds ->
            thresholds.filter { it in MIN_LEVEL..MAX_LEVEL }.sorted() // Validación de rango
        }
        .distinctUntilChanged()
        .stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())

    private val highThresholdsState: StateFlow<List<Int>> = thresholdsDataStore.highThresholds
        .map { thresholds ->
            thresholds.filter { it in MIN_LEVEL..MAX_LEVEL }.sorted() // Validación de rango
        }
        .distinctUntilChanged()
        .stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())

    private val triggeredLevelState: StateFlow<Int> = thresholdsDataStore.triggeredLevel
        .distinctUntilChanged()
        .stateIn(coroutineScope, SharingStarted.Eagerly, -1) // -1 indica que no hay trigger activo

    // Estados públicos para observación
    val lowThresholds: StateFlow<List<Int>> = lowThresholdsState
    val highThresholds: StateFlow<List<Int>> = highThresholdsState
    val currentTriggeredLevel: StateFlow<Int> = triggeredLevelState

    /**
     * Verifica si debe mostrar notificaciones basado en el nivel de batería y estado de carga
     * @param level Nivel actual de batería (0-100)
     * @param isCharging Si el dispositivo está cargando
     * @param onShowNotification Callback para mostrar notificación
     */
    suspend fun checkForNotifications(
        level: Int,
        isCharging: Boolean,
        onShowNotification: (String, String) -> Unit
    ) {
        // Validación de entrada
        if (level !in 0..100) {
            Timber.w("Nivel de batería inválido: $level")
            return
        }

        mutex.withLock {
            val lows = lowThresholdsState.first()
            val highs = highThresholdsState.first()
            val currentTriggered = triggeredLevelState.first()

            Timber.d("Verificando notificaciones - Nivel: $level%, Cargando: $isCharging")
            Timber.d("Umbrales bajos: $lows, altos: $highs, trigger actual: $currentTriggered")

            val triggerToActivate = findNextTrigger(level, isCharging, lows, highs)
            Timber.d("Próximo trigger a activar: $triggerToActivate")

            if (triggerToActivate != null && triggerToActivate != currentTriggered) {
                activateTrigger(triggerToActivate, level, highs, onShowNotification)
            } else if (shouldDeactivateTrigger(level, isCharging, currentTriggered, lows, highs)) {
                Timber.d("Desactivando trigger actual")
                deactivateTrigger()
            }
        }
    }

    /**
     * Encuentra el próximo trigger que debe activarse
     */
    private fun findNextTrigger(
        level: Int,
        isCharging: Boolean,
        lows: List<Int>,
        highs: List<Int>
    ): Int? {
        return when {
            // Descargando: buscar el umbral bajo más cercano por debajo del nivel actual
            !isCharging && lows.isNotEmpty() -> {
                lows.filter { it >= level }.minOrNull()
            }
            // Cargando: buscar el umbral alto más cercano por encima del nivel actual
            isCharging && highs.isNotEmpty() -> {
                highs.filter { it <= level }.maxOrNull()
            }
            else -> null
        }
    }

    /**
     * Determina si debe desactivarse el trigger actual
     */
    private fun shouldDeactivateTrigger(
        level: Int,
        isCharging: Boolean,
        currentTriggered: Int,
        lows: List<Int>,
        highs: List<Int>
    ): Boolean {
        if (currentTriggered == -1) return false

        return when {
            // Si estaba en umbral bajo y ahora está cargando por encima de ese nivel
            currentTriggered in lows && isCharging && level > currentTriggered -> true
            // Si estaba en umbral alto y ahora está descargando por debajo de ese nivel
            currentTriggered in highs && !isCharging && level < currentTriggered -> true
            // Si el trigger actual ya no existe en la configuración
            currentTriggered !in (lows + highs) -> true
            else -> false
        }
    }

    /**
     * Activa un trigger y muestra la notificación correspondiente
     */
    private suspend fun activateTrigger(
        trigger: Int,
        level: Int,
        highs: List<Int>,
        onShowNotification: (String, String) -> Unit
    ) {
        val isHighThreshold = trigger in highs
        val (title, message) = createNotificationContent(level, isHighThreshold)

        Timber.i("Activando trigger: $trigger% (${if (isHighThreshold) "alto" else "bajo"})")

        try {
            thresholdsDataStore.markLevelTriggered(trigger)
            onShowNotification(title, message)
        } catch (e: Exception) {
            Timber.e(e, "Error al activar trigger $trigger")
        }
    }

    /**
     * Desactiva el trigger actual
     */
    private suspend fun deactivateTrigger() {
        Timber.i("Desactivando trigger actual")
        try {
            thresholdsDataStore.markLevelTriggered(-1)
        } catch (e: Exception) {
            Timber.e(e, "Error al desactivar trigger")
        }
    }

    /**
     * Crea el contenido de la notificación
     */
    private fun createNotificationContent(
        currentLevel: Int,
        isHighThreshold: Boolean
    ): Pair<String, String> {
        val titleId = if (isHighThreshold) R.string.battery_fully else R.string.battery_low
        val messageId = when(currentLevel){
            100 -> R.string.battery_level_max
            in 50..99 -> R.string.battery_level_high
            in 16..49 -> R.string.battery_level_low
            else -> R.string.battery_level_critical
        }
        val title = context.getString(titleId)
        val message = context.getString(messageId) +  "$currentLevel%"
        return title to message
    }

    /**
     * Obtiene el estado actual de los triggers de forma síncrona
     */
    fun getCurrentState(): TriggerState {
        return TriggerState(
            lowThresholds = lowThresholdsState.value,
            highThresholds = highThresholdsState.value,
            currentTriggered = triggeredLevelState.value
        )
    }

    /**
     * Clase de datos para representar el estado actual
     */
    data class TriggerState(
        val lowThresholds: List<Int>,
        val highThresholds: List<Int>,
        val currentTriggered: Int
    ) {
        val hasActiveTrigger: Boolean get() = currentTriggered != -1
        val isLowTriggerActive: Boolean get() = currentTriggered in lowThresholds
        val isHighTriggerActive: Boolean get() = currentTriggered in highThresholds
    }
}