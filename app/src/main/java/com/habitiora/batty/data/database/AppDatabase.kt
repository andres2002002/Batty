package com.habitiora.batty.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.habitiora.batty.data.dao.BatteryEntityDao
import com.habitiora.batty.data.entity.BatteryEntity
import com.habitiora.batty.data.database.converters.BatteryTypeConverters

const val DATABASE_NAME = "app_database"
@Database(
    entities = [BatteryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(BatteryTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun batteryDao(): BatteryEntityDao
}