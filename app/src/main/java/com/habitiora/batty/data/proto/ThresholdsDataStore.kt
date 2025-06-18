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
    val triggeredLow: Flow<Map<Int, Boolean>> = dataStore.data.map { it.triggeredLowMap }
    val triggeredHigh: Flow<Map<Int, Boolean>> = dataStore.data.map { it.triggeredHighMap }

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

    // Marcar un umbral como notificado
    suspend fun markLowTriggered(level: Int, triggered: Boolean) {
        dataStore.updateData { current ->
            val builder = current.toBuilder()
            builder.putTriggeredLow(level, triggered)
            builder.build()
        }
    }

    suspend fun markHighTriggered(level: Int, triggered: Boolean) {
        dataStore.updateData { current ->
            val builder = current.toBuilder()
            builder.putTriggeredHigh(level, triggered)
            builder.build()
        }
    }
}