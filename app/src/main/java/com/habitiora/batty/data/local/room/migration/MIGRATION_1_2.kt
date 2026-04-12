package com.habitiora.batty.data.local.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE battery_snapshots ADD COLUMN current_now_ma REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE battery_snapshots ADD COLUMN current_avg_ma REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE battery_snapshots ADD COLUMN charge_counter_mah INTEGER NOT NULL DEFAULT -1")
        db.execSQL("ALTER TABLE battery_snapshots ADD COLUMN watts REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE battery_snapshots ADD COLUMN is_screen_on INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE battery_snapshots ADD COLUMN is_battery_saver INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE battery_snapshots ADD COLUMN is_doze_mode INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE battery_snapshots ADD COLUMN cycle_count INTEGER NOT NULL DEFAULT -1")
        db.execSQL("ALTER TABLE battery_snapshots ADD COLUMN charge_time_remaining_ms INTEGER NOT NULL DEFAULT -1")
        db.execSQL("ALTER TABLE battery_snapshots ADD COLUMN full_capacity_mah INTEGER NOT NULL DEFAULT -1")
        db.execSQL("ALTER TABLE battery_snapshots ADD COLUMN design_capacity_mah INTEGER NOT NULL DEFAULT -1")
    }
}