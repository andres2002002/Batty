package com.habitiora.batty.data.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.habitiora.batty.data.proto.ThresholdsDataStore
import com.habitiora.batty.domain.repository.SettingsRepository
import com.habitiora.batty.services.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class SettingsRepositoryImp @Inject constructor(
    private val thresholdsDataStore: ThresholdsDataStore,
    private val settingsDataStore: SettingsDataStore
): SettingsRepository {
    companion object{
        private val MonitorEnabledKey = booleanPreferencesKey(SettingsDataStore.BATTERY_MONITOR_ENABLED)
        private val NotificationEnabledKey = booleanPreferencesKey(SettingsDataStore.BATTERY_NOTIFICATION_ENABLED)
        private val DndEnabledKey = booleanPreferencesKey(SettingsDataStore.BATTERY_NOTIFICATIONS_DND)

        private const val MIN_LEVEL = 1
        private const val MAX_LEVEL = 100
    }
    val dataStore = settingsDataStore.dataStore

    override val isBatteryMonitorEnabled: Flow<Boolean> = dataStore.data
        .map {
            it[MonitorEnabledKey] ?:true
        }
        .distinctUntilChanged()

    override suspend fun setBatteryMonitorEnabled(enabled: Boolean): Boolean {
        try {
            dataStore.edit{
                it[MonitorEnabledKey] = enabled
            }
            Timber.d("Battery monitor enabled state set to $enabled")
            return true
        }
        catch (e: Exception){
            Timber.e(e,"Error setting battery monitor enabled state")
            return false
        }
    }

    override val isBatteryNotificationEnabled: Flow<Boolean> = dataStore.data
        .map {
            it[NotificationEnabledKey] ?:true
        }
        .distinctUntilChanged()

    override suspend fun setBatteryNotificationEnabled(enabled: Boolean): Boolean {
        try {
            dataStore.edit{
                it[NotificationEnabledKey] = enabled
            }
            Timber.d("Battery notification enabled state set to $enabled")
            return true
        }
        catch (e: Exception){
            Timber.e(e,"Error setting battery notification enabled state")
            return false
        }
    }

    override val isNotificationsDNDEnabled: Flow<Boolean> = dataStore.data
        .map {
            it[DndEnabledKey] ?:false
        }
        .distinctUntilChanged()

    override suspend fun setNotificationsDNDEnabled(enabled: Boolean): Boolean {
        try {
            dataStore.edit{
                it[DndEnabledKey] = enabled
            }
            Timber.d("Battery notifications DND enabled state set to $enabled")
            return true
        }
        catch (e: Exception){
            Timber.e(e,"Error setting battery notifications DND enabled state")
            return false
        }
    }

    override val batteryThresholdsUnder: Flow<List<Int>> = thresholdsDataStore.lowThresholds
        .map { thresholds ->
            thresholds.filter { it in MIN_LEVEL..MAX_LEVEL }.sorted() // Validación de rango
        }
        .distinctUntilChanged()

    override suspend fun addBatteryThresholdsUnder(level: Int): Boolean {
        val thresholds = thresholdsDataStore.lowThresholds.first()
        return addBatteryThreshold(thresholds, level) {
            thresholdsDataStore.updateLowThresholds(it)
        }
    }

    override suspend fun updateBatteryThresholdsUnder(currentLevel: Int, newLevel: Int): Boolean{
        val thresholds = thresholdsDataStore.lowThresholds.first()
        return updateBatteryThresholds(thresholds, currentLevel, newLevel) {
            thresholdsDataStore.updateLowThresholds(it)
        }
    }

    override suspend fun removeBatteryThresholdsUnder(level: Int): Boolean {
        val thresholds = thresholdsDataStore.lowThresholds.first()
        return removeBatteryThreshold(thresholds, level) {
            thresholdsDataStore.updateLowThresholds(it)
        }
    }

    override val batteryThresholdsOver: Flow<List<Int>> = thresholdsDataStore.highThresholds
        .map { thresholds ->
            thresholds.filter { it in MIN_LEVEL..MAX_LEVEL }.sorted() // Validación de rango
        }
        .distinctUntilChanged()

    override suspend fun addBatteryThresholdsOver(level: Int): Boolean {
        val thresholds = thresholdsDataStore.highThresholds.first()
        return addBatteryThreshold(thresholds, level) {
            thresholdsDataStore.updateHighThresholds(it)
        }
    }

    override suspend fun updateBatteryThresholdsOver(currentLevel: Int, newLevel: Int): Boolean{
        val thresholds = thresholdsDataStore.highThresholds.first()
        return updateBatteryThresholds(thresholds, currentLevel, newLevel) {
            thresholdsDataStore.updateHighThresholds(it)
        }
    }

    override suspend fun removeBatteryThresholdsOver(level: Int): Boolean {
        val thresholds = thresholdsDataStore.highThresholds.first()
        return removeBatteryThreshold(thresholds, level) {
            thresholdsDataStore.updateHighThresholds(it)
        }
    }

    private suspend fun addBatteryThreshold(thresholds: List<Int>, level: Int, onAdd: suspend (List<Int>) -> Unit): Boolean {
        try {
            val newThresholds = thresholds.toMutableList()
            newThresholds.add(level)
            onAdd(newThresholds)
            Timber.d("Battery threshold added: $level")
            return true
        } catch (e: Exception) {
            Timber.e(e, "Error adding battery threshold")
            return false
        }
    }

    private suspend fun updateBatteryThresholds(
        thresholds: List<Int>,
        currentLevel: Int,
        newLevel: Int,
        onUpdate: suspend (List<Int>) -> Unit
    ): Boolean{
        try {
            val newThresholds = thresholds.toMutableList()
            val index = newThresholds.indexOf(currentLevel)
            if (index == -1) return true
            newThresholds[index] = newLevel
            onUpdate(newThresholds)
            Timber.d("Battery threshold updated: $currentLevel -> $newLevel")
            return true
        } catch (e: Exception) {
            Timber.e(e, "Error updating battery threshold")
            return false
        }
    }

    private suspend fun removeBatteryThreshold(thresholds: List<Int>, level: Int, onRemove: suspend (List<Int>) -> Unit): Boolean {
        try {
            val newThresholds = thresholds.toMutableList()
            if (!newThresholds.remove(level)) return true
            onRemove(newThresholds)
            Timber.d("Battery threshold removed: $level")
            return true
        } catch (e: Exception) {
            Timber.e(e, "Error removing battery threshold")
            return false
        }
    }

}