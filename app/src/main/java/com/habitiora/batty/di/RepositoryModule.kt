package com.habitiora.batty.di

import com.habitiora.batty.data.dao.BatteryEntityDao
import com.habitiora.batty.data.proto.ThresholdsDataStore
import com.habitiora.batty.data.repository.BatteryEntityRepository
import com.habitiora.batty.data.repository.BatteryEntityRepositoryImpl
import com.habitiora.batty.data.repository.SettingsRepositoryImp
import com.habitiora.batty.domain.repository.SettingsRepository
import com.habitiora.batty.services.SettingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideBatteryEntityRepository(batteryEntityDao: BatteryEntityDao): BatteryEntityRepository =
        BatteryEntityRepositoryImpl(batteryEntityDao)

    @Provides
    @Singleton
    fun providesSettingsRepository(
        thresholdsDataStore: ThresholdsDataStore,
        settingsDataStore: SettingsDataStore
    ): SettingsRepository =
        SettingsRepositoryImp(thresholdsDataStore, settingsDataStore)

}