package com.habitiora.batty.domain.useCase

import com.habitiora.batty.domain.model.ThresholdsConfig
import com.habitiora.batty.domain.repository.ThresholdsRepository
import javax.inject.Inject

class UpdateThresholdsConfigUseCase @Inject constructor(
    private val repository: ThresholdsRepository
) {
    suspend operator fun invoke(thresholds: ThresholdsConfig) = repository.update(thresholds)
    suspend fun setLowThresholds(thresholds: List<Int>) = repository.setLowThresholds(thresholds)
    suspend fun setHighThresholds(thresholds: List<Int>) = repository.setHighThresholds(thresholds)
    suspend fun setTriggeredLevel(level: Int) = repository.setTriggeredLevel(level)
    suspend fun removeLowThreshold(value: Int) = repository.removeLowThreshold(value)
    suspend fun removeHighThreshold(value: Int) = repository.removeHighThreshold(value)
    suspend fun addLowThreshold(value: Int) = repository.addLowThreshold(value)

    suspend fun addHighThreshold(value: Int) = repository.addHighThreshold(value)

    suspend fun updateLowThreshold(oldValue: Int, newValue: Int) = repository.updateLowThreshold(oldValue, newValue)

    suspend fun updateHighThreshold(oldValue: Int, newValue: Int) = repository.updateHighThreshold(oldValue, newValue)
}