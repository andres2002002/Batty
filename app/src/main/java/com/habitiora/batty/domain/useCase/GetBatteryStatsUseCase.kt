package com.habitiora.batty.domain.useCase

import com.habitiora.batty.domain.model.BatteryStats
import com.habitiora.batty.domain.model.TimeRange
import com.habitiora.batty.domain.repository.BatteryRepository
import javax.inject.Inject

class GetBatteryStatsUseCase @Inject constructor(
    private val repository: BatteryRepository
) {
    suspend operator fun invoke(timeRange: TimeRange): BatteryStats = repository.getStats(timeRange)
}