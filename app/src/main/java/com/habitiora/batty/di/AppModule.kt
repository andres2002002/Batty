package com.habitiora.batty.di

import android.app.NotificationManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.habitiora.batty.data.datastore.MonitorSettingsSerializer
import com.habitiora.batty.domain.model.ThresholdsConfig
import com.habitiora.batty.data.datastore.ThresholdsSerializer
import com.habitiora.batty.domain.model.MonitorSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideThresholdsDataStore(
        @ApplicationContext context: Context
    ): DataStore<ThresholdsConfig> {
        return DataStoreFactory.create(
            serializer = ThresholdsSerializer,
            produceFile = { context.dataStoreFile("thresholds_config.json") }
        )
    }

    @Provides
    @Singleton
    fun providesMonitorSettingsDataStore(
        @ApplicationContext context: Context
    ): DataStore<MonitorSettings> {
        return DataStoreFactory.create(
            serializer = MonitorSettingsSerializer,
            produceFile = { context.dataStoreFile("monitor_settings.json") }
        )
    }

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager =
        context.getSystemService(NotificationManager::class.java)
}
