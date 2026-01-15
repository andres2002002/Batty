package com.habitiora.batty.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import com.habitiora.batty.data.dao.BatteryEntityDao
import com.habitiora.batty.data.database.AppDatabase
import com.habitiora.batty.data.database.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataBaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(application: Application): AppDatabase{
        return Room.databaseBuilder(application, AppDatabase::class.java, DATABASE_NAME)
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideBatteryDao(appDatabase: AppDatabase): BatteryEntityDao = appDatabase.batteryDao()
}