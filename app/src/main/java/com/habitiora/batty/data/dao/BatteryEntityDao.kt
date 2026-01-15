package com.habitiora.batty.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.habitiora.batty.data.entity.BatteryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatteryState(state: BatteryEntity)

    @Query("SELECT * FROM battery_state_table ORDER BY timestamp DESC LIMIT 1")
    fun getLastBatteryState(): Flow<BatteryEntity?>

    @Query("SELECT * FROM battery_state_table ORDER BY timestamp DESC")
    fun getAllBatteryStates(): Flow<List<BatteryEntity>>

    @Query("SELECT * FROM battery_state_table WHERE timestamp >= :timestamp ORDER BY timestamp DESC")
    fun getBatteryStatesSince(timestamp: Long): Flow<List<BatteryEntity>>

    @Query("SELECT * FROM battery_state_table WHERE session_id = :sessionId ORDER BY timestamp DESC")
    fun getBatteryStatesBySessionId(sessionId: String): Flow<List<BatteryEntity>>

    @Query("DELETE FROM battery_state_table WHERE timestamp < :timestamp")
    suspend fun deleteBatteryStateSince(timestamp: Long)

    @Query("DELETE FROM battery_state_table")
    suspend fun clearAll()
}