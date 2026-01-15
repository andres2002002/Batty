package com.habitiora.batty.di

import com.habitiora.batty.data.manager.BatteryTimeEstimator
import com.habitiora.batty.data.manager.RegisterReceiverManager
import com.habitiora.batty.data.manager.SessionManager
import com.habitiora.batty.services.BatteryReceiver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BatteryNotifyModule {
    @Provides
    @Singleton
    fun provideSessionManager(): SessionManager = SessionManager()

    @Provides
    @Singleton
    fun provideBatteryTimeEstimator(): BatteryTimeEstimator = BatteryTimeEstimator()

    @Provides
    @Singleton
    fun provideBatteryReceiver(batteryEstimator: BatteryTimeEstimator, sessionManager: SessionManager): BatteryReceiver =
        BatteryReceiver(batteryEstimator, sessionManager)

    @Singleton
    @Provides
    fun provideRegisterReceiverManager(batteryReceiver: BatteryReceiver): RegisterReceiverManager =
        RegisterReceiverManager(batteryReceiver)

}