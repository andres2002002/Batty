package com.habitiora.batty.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import com.habitiora.batty.data.local.room.DatabaseContracts
import com.habitiora.batty.data.local.room.dao.BatteryDao
import com.habitiora.batty.data.local.room.database.AppDatabase
import com.habitiora.batty.data.local.room.migration.MIGRATION_1_2
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
        return Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            DatabaseContracts.DATABASE_NAME)
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideBatteryDao(appDatabase: AppDatabase): BatteryDao = appDatabase.batteryDao()
}