package com.habitiora.batty.data.proto

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThresholdsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore: DataStore<ThresholdsConfig> = DataStoreFactory.create(
        serializer = ThresholdsSerializer,
        produceFile = { context.dataStoreFile("thresholds_config.pb") }
    )

    // Flow de listas de umbrales
    val lowThresholds: Flow<List<Int>> = dataStore.data.map { it.lowThresholdsList }
    val highThresholds: Flow<List<Int>> = dataStore.data.map { it.highThresholdsList }

    // Flow de estados de notificación
    val triggeredLevel: Flow<Int> = dataStore.data.map { it.triggeredLevel }

    // Guardar nuevos umbrales (si quieres actualizar las listas)
    suspend fun updateThresholds(low: List<Int>, high: List<Int>) {
        dataStore.updateData { current ->
            current.toBuilder()
                .clearLowThresholds()
                .addAllLowThresholds(low)
                .clearHighThresholds()
                .addAllHighThresholds(high)
                .build()
        }
    }

    suspend fun updateLowThresholds(low: List<Int>) {
        dataStore.updateData { current ->
            current.toBuilder()
                .clearLowThresholds()
                .addAllLowThresholds(low)
                .build()
        }
    }

    suspend fun updateHighThresholds(high: List<Int>) {
        dataStore.updateData { current ->
            current.toBuilder()
                .clearHighThresholds()
                .addAllHighThresholds(high)
                .build()
        }
    }

    // Marcar un umbral como notificado
    suspend fun markLevelTriggered(level: Int) {
        dataStore.updateData { current ->
            val builder = current.toBuilder()
            builder.setTriggeredLevel(level)
            builder.build()
        }
    }
}