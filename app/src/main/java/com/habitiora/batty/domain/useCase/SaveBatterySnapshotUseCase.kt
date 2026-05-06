package com.habitiora.batty.domain.useCase

import com.habitiora.batty.domain.model.BatteryInfo
import com.habitiora.batty.domain.repository.BatteryRepository
import javax.inject.Inject

class SaveBatterySnapshotUseCase @Inject constructor(
    private val repository: BatteryRepository
) {
    suspend operator fun invoke(batteryInfo: BatteryInfo) = repository.saveSnapshot(batteryInfo)
}