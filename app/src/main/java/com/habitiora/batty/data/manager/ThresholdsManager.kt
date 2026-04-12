package com.habitiora.batty.data.manager

import com.habitiora.batty.domain.model.ThresholdEvent
import com.habitiora.batty.domain.model.ThresholdState
import com.habitiora.batty.domain.model.ThresholdsConfig
import com.habitiora.batty.domain.repository.ThresholdsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThresholdsManager @Inject constructor(
    private val thresholdsRepository: ThresholdsRepository
) {
    private val triggeredLowLevels = mutableSetOf<Int>()
    private val triggeredHighLevels = mutableSetOf<Int>()
    private var previousIsCharging: Boolean? = null

    private val _state = MutableStateFlow(ThresholdState())
    val state: StateFlow<ThresholdState> = _state.asStateFlow()

    fun getCurrentState(): ThresholdState = _state.value

    suspend fun evaluate(level: Int, isCharging: Boolean): ThresholdEvent? {
        val config = thresholdsRepository.get()
        handleChargingStateTransition(isCharging)
        return if (!isCharging) {
            evaluateLowThresholds(level, config)
        } else {
            evaluateHighThresholds(level, config.highThresholds)
        }
    }

    private fun handleChargingStateTransition(isCharging: Boolean) {
        if (previousIsCharging == isCharging) return
        when {
            isCharging -> {
                triggeredLowLevels.clear()
                _state.update { it.copy(hasActiveLowTrigger = false, activeLevel = -1) }
                Timber.d("ThresholdsManager → charging started, low triggers cleared")
            }

            else -> {
                triggeredHighLevels.clear()
                _state.update { it.copy(hasActiveHighTrigger = false, activeLevel = -1) }
                Timber.d("ThresholdsManager → discharging started, high triggers cleared")
            }
        }
        previousIsCharging = isCharging
    }

    private fun evaluateLowThresholds(level: Int, config: ThresholdsConfig): ThresholdEvent? {
        // Todos los thresholds cruzados en este momento que aún no se han notificado.
        // Ejemplo: level=7, thresholds=[20,15,10,4] → newlyCrossed=[20,15,10]
        val newlyCrossed = config.lowThresholds
            .filter { threshold -> level <= threshold && threshold !in triggeredLowLevels }

        if (newlyCrossed.isEmpty()) return null

        // Marcar TODOS como triggered de una sola vez para evitar notificaciones repetidas
        // en broadcasts subsiguientes con el mismo nivel.
        triggeredLowLevels.addAll(newlyCrossed)
        _state.update { it.copy(hasActiveLowTrigger = true, activeLevel = level) }

        Timber.i(
            "Low thresholds crossed → level=$level% | " +
                    "newlyCrossed=$newlyCrossed | allTriggered=$triggeredLowLevels"
        )

        // Notificar solo una vez con el nivel actual — la severidad la determina el nivel,
        // no cuántos thresholds se cruzaron.
        return ThresholdEvent.LowBattery(
            level = level,
            isCritical = config.isCriticalLevel(level),
            isVeryLow = config.isVeryLowLevel(level)
        )
    }

    private fun evaluateHighThresholds(level: Int, thresholds: List<Int>): ThresholdEvent? {
        // Mismo patrón: marcar todos los alcanzados de una vez
        val newlyReached = thresholds
            .filter { threshold -> level >= threshold && threshold !in triggeredHighLevels }

        if (newlyReached.isEmpty()) return null

        triggeredHighLevels.addAll(newlyReached)
        _state.update { it.copy(hasActiveHighTrigger = true, activeLevel = level) }

        Timber.i(
            "High thresholds reached → level=$level% | " +
                    "newlyReached=$newlyReached | allTriggered=$triggeredHighLevels"
        )

        return ThresholdEvent.HighBattery(
            level = level,
            isFullyCharged = level >= 100
        )
    }
}