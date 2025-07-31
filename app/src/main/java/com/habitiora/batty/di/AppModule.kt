package com.habitiora.batty.di

import android.app.NotificationManager
import android.content.Context
import com.habitiora.batty.data.proto.ThresholdsDataStore
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
    fun provideThresholdsDataStore(@ApplicationContext context: Context): ThresholdsDataStore =
        ThresholdsDataStore(context)

    @Provides
    @Singleton
    fun provideBatteryHistoryStrategy(): BatteryHistoryStrategy = BatteryHistoryStrategy()
}
