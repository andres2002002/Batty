package com.habitiora.batty.data.repository

import com.habitiora.batty.core.toDomain
import com.habitiora.batty.core.toEntity
import com.habitiora.batty.data.local.room.dao.BatteryDao
import com.habitiora.batty.data.source.BatteryInfoSource
import com.habitiora.batty.domain.model.BatteryDataPoint
import com.habitiora.batty.domain.model.BatteryInfo
import com.habitiora.batty.domain.model.BatteryStats
import com.habitiora.batty.domain.model.LevelDataPoint
import com.habitiora.batty.domain.model.TimeRange
import com.habitiora.batty.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BatteryRepositoryImpl @Inject constructor(
    private val dao: BatteryDao,
    private val batteryInfoSource: BatteryInfoSource
) : BatteryRepository {

    override fun observeLiveBattery(): Flow<BatteryInfo> = batteryInfoSource.observeLive()

    override fun observeLatestSnapshot(): Flow<BatteryInfo?> =
        dao.observeLatest().map { it?.toDomain() }

    override fun observeChartData(timeRange: TimeRange): Flow<List<BatteryDataPoint>> {
        val since = timeRange.toSinceMillis()
        return dao.observeChartDataSince(since).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveSnapshot(batteryInfo: BatteryInfo) {
        dao.insert(batteryInfo.toEntity())
        Timber.d("Snapshot saved → ${batteryInfo.level}% · ${batteryInfo.temperature}°C · ${batteryInfo.status}")
    }

    override suspend fun getStats(timeRange: TimeRange): BatteryStats {
        val since = timeRange.toSinceMillis()
        return dao.getStatsSince(since).toDomain()
    }

    override suspend fun cleanOldData(keepDays: Int) {
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(keepDays.toLong())
        dao.deleteOlderThan(cutoff)
        Timber.d("Cleaned data older than $keepDays days")
    }

    private fun TimeRange.toSinceMillis(): Long =
        System.currentTimeMillis() - TimeUnit.HOURS.toMillis(hours.toLong())
}