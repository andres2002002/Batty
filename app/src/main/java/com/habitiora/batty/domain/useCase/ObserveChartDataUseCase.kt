package com.habitiora.batty.domain.useCase

import com.habitiora.batty.domain.model.BatteryDataPoint
import com.habitiora.batty.domain.model.TimeRange
import com.habitiora.batty.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveChartDataUseCase @Inject constructor(
    private val repository: BatteryRepository
) {
    operator fun invoke(timeRange: TimeRange): Flow<List<BatteryDataPoint>> =
        repository.observeChartData(timeRange)
}