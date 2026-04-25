package com.habitiora.batty.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.habitiora.batty.data.local.room.entity.BatteryEntity
import com.habitiora.batty.data.local.room.pojo.BatteryDataPointEntity
import com.habitiora.batty.data.local.room.pojo.BatteryStatsSummary
import kotlinx.coroutines.flow.Flow


@Dao
interface BatteryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BatteryEntity): Long

    @Query("SELECT * FROM battery_snapshots ORDER BY timestamp DESC LIMIT 1")
    fun observeLatest(): Flow<BatteryEntity?>

    @Query("""
        SELECT
            AVG(level)                  AS avgLevel,
            MIN(level)                  AS minLevel,
            MAX(level)                  AS maxLevel,
            AVG(temperature)            AS avgTemperature,
            MIN(temperature)            AS minTemperature,
            MAX(temperature)            AS maxTemperature,
            AVG(voltage)                AS avgVoltage,
            AVG(ABS(current_now_ma))    AS avgCurrentMa,
            MAX(ABS(current_now_ma))    AS peakCurrentMa,
            AVG(watts)                  AS avgWatts,
            MAX(watts)                  AS peakWatts,
            SUM(is_screen_on)           AS screenOnSamples,
            SUM(is_battery_saver)       AS batterySaverSamples,
            COUNT(*)                    AS totalSamples
        FROM battery_snapshots
        WHERE timestamp >= :since
    """)
    suspend fun getStatsSince(since: Long): BatteryStatsSummary

    @Query("""
    SELECT
        timestamp,
        level,
        temperature,
        ABS(current_now_ma) AS currentNowMa,
        watts
    FROM battery_snapshots
    WHERE timestamp >= :since
    ORDER BY timestamp ASC
""")
    fun observeChartDataSince(since: Long): Flow<List<BatteryDataPointEntity>>

    @Query("DELETE FROM battery_snapshots WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("SELECT COUNT(*) FROM battery_snapshots")
    suspend fun count(): Int
}