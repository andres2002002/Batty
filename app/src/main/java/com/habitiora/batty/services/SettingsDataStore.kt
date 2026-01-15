package com.habitiora.batty.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStore @Inject constructor(context: Context) {
    companion object {
        const val DATASTORE_SETTINGS = "Settings_Data"
        const val BATTERY_MONITOR_ENABLED = "Battery_Monitor_Enabled"
        const val BATTERY_NOTIFICATION_ENABLED = "Battery_Notification_Enabled"

        const val BATTERY_NOTIFICATIONS_DND = "Battery_Notifications_DND"
    }
    private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_SETTINGS)
    val dataStore = context.settingsDataStore
}