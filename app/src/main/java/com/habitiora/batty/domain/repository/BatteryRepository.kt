package com.habitiora.batty.domain.repository

import com.habitiora.batty.domain.model.BatteryDataPoint
import com.habitiora.batty.domain.model.BatteryInfo
import com.habitiora.batty.domain.model.BatteryStats
import com.habitiora.batty.domain.model.LevelDataPoint
import com.habitiora.batty.domain.model.TimeRange
import kotlinx.coroutines.flow.Flow

interface BatteryRepository {
    fun observeLiveBattery(): Flow<BatteryInfo>
    fun observeLatestSnapshot(): Flow<BatteryInfo?>
    fun observeChartData(timeRange: TimeRange): Flow<List<BatteryDataPoint>>
    suspend fun saveSnapshot(batteryInfo: BatteryInfo)
    suspend fun getStats(timeRange: TimeRange): BatteryStats
    suspend fun cleanOldData(keepDays: Int = 30)
}