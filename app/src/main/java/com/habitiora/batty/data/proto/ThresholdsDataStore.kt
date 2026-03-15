package com.habitiora.batty.data.proto

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.habitiora.batty.data.datastore.ThresholdsConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThresholdsDataStore @Inject constructor(
    private val dataStore: DataStore<ThresholdsConfig>
) {


    // Flow de listas de umbrales
    val lowThresholds: Flow<List<Int>> = dataStore.data.map { it.lowThresholds }
    val highThresholds: Flow<List<Int>> = dataStore.data.map { it.highThresholds }

    // Flow de estados de notificación
    val triggeredLevel: Flow<Int> = dataStore.data.map { it.triggeredLevel }

    suspend fun updateThresholds(low: List<Int>, high: List<Int>) {
        dataStore.updateData { current ->
            current.copy(
                lowThresholds = low,
                highThresholds = high
            )
        }
    }

    suspend fun updateLowThresholds(low: List<Int>) {
        dataStore.updateData { current ->
            current.copy(lowThresholds = low)
        }
    }

    suspend fun updateHighThresholds(high: List<Int>) {
        dataStore.updateData { current ->
            current.copy(highThresholds = high)
        }
    }

    suspend fun markLevelTriggered(level: Int) {
        dataStore.updateData { current ->
            current.copy(triggeredLevel = level)
        }
    }
}