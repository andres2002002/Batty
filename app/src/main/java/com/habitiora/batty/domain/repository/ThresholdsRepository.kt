package com.habitiora.batty.domain.repository

import com.habitiora.batty.domain.model.ThresholdsConfig
import kotlinx.coroutines.flow.Flow

interface ThresholdsRepository {
    fun observe(): Flow<ThresholdsConfig>
    suspend fun get(): ThresholdsConfig
    suspend fun update(config: ThresholdsConfig)
    suspend fun setLowThresholds(thresholds: List<Int>)
    suspend fun setHighThresholds(thresholds: List<Int>)
    suspend fun setTriggeredLevel(level: Int)
    suspend fun removeLowThreshold(value: Int)
    suspend fun removeHighThreshold(value: Int)
    suspend fun addLowThreshold(value: Int)
    suspend fun addHighThreshold(value: Int)
    suspend fun updateLowThreshold(oldValue: Int, newValue: Int)
    suspend fun updateHighThreshold(oldValue: Int, newValue: Int)
}