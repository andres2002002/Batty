package com.habitiora.batty.data.manager

import com.habitiora.batty.data.repository.BatteryEntityRepository
import com.habitiora.batty.domain.model.BatteryState
import com.habitiora.batty.utils.BatteryHistoryStrategy
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatterySaveManager @Inject constructor(
    private val batteryEntityRepository: BatteryEntityRepository,
    private val batteryHistoryStrategy: BatteryHistoryStrategy
){
    var lastBatteryState = BatteryState()

    suspend fun saveBatteryState(state: BatteryState) {
        val shouldSave = batteryHistoryStrategy.shouldSaveEntry(
            state.batteryLevel,
            lastBatteryState.batteryLevel,
            state.isCharging,
            lastBatteryState.isCharging,
            lastBatteryState.timestamp,
            state.temperature,
            lastBatteryState.temperature,
            state.screenOn
        )
        if (shouldSave) {
            Timber.d("Saving battery state: $state")
            batteryEntityRepository.insertBatteryState(state)
            lastBatteryState = state.copy()
        }
    }


}