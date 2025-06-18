package com.habitiora.batty.data.repository

import com.habitiora.batty.data.proto.ThresholdsDataStore
import com.habitiora.batty.domain.repository.SettingsRepository
import javax.inject.Inject

class SettingsRepositoryImp @Inject constructor(
    private val thresholdsDataStore: ThresholdsDataStore
): SettingsRepository {
    override fun isBatteryMonitorEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setBatteryMonitorEnabled(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getBatteryLevelUnder(): Int {
        TODO("Not yet implemented")
    }

    override fun setBatteryLevelUnder(level: Int) {
        TODO("Not yet implemented")
    }

    override fun getBatteryLevelOver(): Int {
        TODO("Not yet implemented")
    }

    override fun setBatteryLevelOver(level: Int) {
        TODO("Not yet implemented")
    }
}