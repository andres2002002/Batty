package com.habitiora.batty.data.repository

import androidx.datastore.core.DataStore
import com.habitiora.batty.domain.model.ThresholdsConfig
import com.habitiora.batty.domain.repository.ThresholdsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThresholdsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<ThresholdsConfig>
) : ThresholdsRepository {
    override fun observe(): Flow<ThresholdsConfig> = dataStore.data

    override suspend fun get(): ThresholdsConfig = observe().first()

    override suspend fun update(config: ThresholdsConfig) {
        dataStore.updateData { _ -> config }
        Timber.d("ThresholdsConfig saved: low=${config.lowThresholds} high=${config.highThresholds}")
    }

    override suspend fun setLowThresholds(thresholds: List<Int>) {
        dataStore.updateData { current ->
            current.copy(lowThresholds = thresholds)
        }
    }

    override suspend fun setHighThresholds(thresholds: List<Int>) {
        dataStore.updateData { current ->
            current.copy(highThresholds = thresholds)
        }
    }

    override suspend fun setTriggeredLevel(level: Int) {
        dataStore.updateData { current ->
            current.copy(triggeredLevel = level)
        }
    }
    override suspend fun removeLowThreshold(value: Int) {
        dataStore.updateData { current ->
            current.copy(lowThresholds = current.lowThresholds.filterNot { it == value })
        }
    }

    override suspend fun removeHighThreshold(value: Int) {
        dataStore.updateData { current ->
            current.copy(highThresholds = current.highThresholds.filterNot { it == value })
        }
    }

    override suspend fun addLowThreshold(value: Int) {
        dataStore.updateData { current ->
            if (!current.lowThresholds.contains(value)) {
                current.copy(lowThresholds = (current.lowThresholds + value).sorted())
            } else current
        }
    }

    override suspend fun addHighThreshold(value: Int) {
        dataStore.updateData { current ->
            if (!current.highThresholds.contains(value)) {
                current.copy(highThresholds = (current.highThresholds + value).sorted())
            } else current
        }
    }

    override suspend fun updateLowThreshold(oldValue: Int, newValue: Int) {
        dataStore.updateData { current ->
            current.copy(
                lowThresholds = current.lowThresholds.map {
                    if (it == oldValue) newValue else it
                }.distinct().sorted()
            )
        }
    }

    override suspend fun updateHighThreshold(oldValue: Int, newValue: Int) {
        dataStore.updateData { current ->
            current.copy(
                highThresholds = current.highThresholds.map {
                    if (it == oldValue) newValue else it
                }.distinct().sorted()
            )
        }
    }
}