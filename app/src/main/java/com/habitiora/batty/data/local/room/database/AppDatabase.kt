package com.habitiora.batty.data.local.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.habitiora.batty.data.local.room.dao.BatteryDao
import com.habitiora.batty.data.local.room.database.converters.BatteryTypeConverters
import com.habitiora.batty.data.local.room.entity.BatteryEntity

@Database(
    entities = [BatteryEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(BatteryTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun batteryDao(): BatteryDao
}