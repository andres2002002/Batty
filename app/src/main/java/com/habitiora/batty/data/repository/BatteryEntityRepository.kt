package com.habitiora.batty.data.repository

import com.habitiora.batty.data.converters.toBatteryEntity
import com.habitiora.batty.data.converters.toBatteryState
import com.habitiora.batty.data.dao.BatteryEntityDao
import com.habitiora.batty.data.entity.BatteryEntity
import com.habitiora.batty.domain.model.BatteryState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface BatteryEntityRepository {
    fun getLastBatteryState(): Flow<BatteryState?>
    suspend fun insertBatteryState(state: BatteryState)
    fun getAllBatteryStates(): Flow<List<BatteryState>>

    fun getBatteryStatesSince(timestamp: Long): Flow<List<BatteryState>>
    fun getBatteryStatesBySessionId(sessionId: String): Flow<List<BatteryState>>
    suspend fun deleteBatteryStateSince(timestamp: Long)
}

@Singleton
class BatteryEntityRepositoryImpl @Inject constructor(
    private val batteryEntityDao: BatteryEntityDao
) : BatteryEntityRepository{

    private fun Flow<List<BatteryEntity>>.toBatteryStateList(): Flow<List<BatteryState>> =
        this.map { list -> list.map { it.toBatteryState() } }

    override fun getLastBatteryState(): Flow<BatteryState?> =
        batteryEntityDao.getLastBatteryState().map { it?.toBatteryState() }

    override suspend fun insertBatteryState(state: BatteryState) {
        batteryEntityDao.insertBatteryState(state.toBatteryEntity())
    }

    override fun getAllBatteryStates(): Flow<List<BatteryState>> =
        batteryEntityDao.getAllBatteryStates().toBatteryStateList()

    override fun getBatteryStatesSince(timestamp: Long): Flow<List<BatteryState>> =
        batteryEntityDao.getBatteryStatesSince(timestamp).toBatteryStateList()

    override fun getBatteryStatesBySessionId(sessionId: String): Flow<List<BatteryState>> =
        batteryEntityDao.getBatteryStatesBySessionId(sessionId).toBatteryStateList()

    override suspend fun deleteBatteryStateSince(timestamp: Long) {
        batteryEntityDao.deleteBatteryStateSince(timestamp)
    }
}