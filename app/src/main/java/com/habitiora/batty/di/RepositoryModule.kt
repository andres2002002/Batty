package com.habitiora.batty.di

import com.habitiora.batty.data.manager.BatteryServiceControllerImpl
import com.habitiora.batty.data.repository.BatteryRepositoryImpl
import com.habitiora.batty.data.repository.SettingsRepositoryImpl
import com.habitiora.batty.data.repository.ThresholdsRepositoryImpl
import com.habitiora.batty.domain.controller.BatteryServiceController
import com.habitiora.batty.domain.repository.BatteryRepository
import com.habitiora.batty.domain.repository.SettingsRepository
import com.habitiora.batty.domain.repository.ThresholdsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBatteryRepository(impl: BatteryRepositoryImpl): BatteryRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindThresholdsRepository(impl: ThresholdsRepositoryImpl): ThresholdsRepository

    @Binds
    @Singleton
    abstract fun bindBatteryServiceController(
        impl: BatteryServiceControllerImpl
    ): BatteryServiceController
}