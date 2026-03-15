package com.habitiora.batty.di

import android.app.NotificationManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.habitiora.batty.data.datastore.ThresholdsConfig
import com.habitiora.batty.data.proto.ThresholdsDataStore
import com.habitiora.batty.data.proto.ThresholdsSerializer
import com.habitiora.batty.services.NotificationHelper
import com.habitiora.batty.services.SettingsDataStore
import com.habitiora.batty.utils.BatteryHistoryStrategy
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
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context,
        notificationManager: NotificationManager
    ): NotificationHelper =
        NotificationHelper(context, notificationManager)

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore =
        SettingsDataStore(context)

    @Provides
    @Singleton
    fun provideBatteryHistoryStrategy(): BatteryHistoryStrategy = BatteryHistoryStrategy()
}
