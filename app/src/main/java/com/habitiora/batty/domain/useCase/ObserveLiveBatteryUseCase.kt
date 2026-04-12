package com.habitiora.batty.domain.useCase

import com.habitiora.batty.domain.model.BatteryInfo
import com.habitiora.batty.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLiveBatteryUseCase @Inject constructor(
    private val repository: BatteryRepository
) {
    operator fun invoke(): Flow<BatteryInfo> = repository.observeLiveBattery()
}