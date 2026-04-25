package com.habitiora.batty.data.local.room.entity

import  androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.habitiora.batty.data.local.room.DatabaseContracts.Battery

@Entity(
    tableName = Battery.TABLE_NAME,
    indices = [Index(value = [Battery.COLUMN_TIMESTAMP])]
)
data class BatteryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Battery.COLUMN_ID) val id: Long = 0L,
    @ColumnInfo(name = Battery.COLUMN_LEVEL) val level: Int,
    @ColumnInfo(name = Battery.COLUMN_STATUS) val status: String,
    @ColumnInfo(name = Battery.COLUMN_HEALTH) val health: String,
    @ColumnInfo(name = Battery.COLUMN_PLUGGED) val plugged: String,
    @ColumnInfo(name = Battery.COLUMN_TEMPERATURE) val temperature: Float,
    @ColumnInfo(name = Battery.COLUMN_VOLTAGE) val voltage: Int,
    @ColumnInfo(name = Battery.COLUMN_TECHNOLOGY) val technology: String,
    @ColumnInfo(name = Battery.COLUMN_TIMESTAMP) val timestamp: Long,
    @ColumnInfo(name = Battery.COLUMN_CURRENT_NOW_MA) val currentNowMa: Float,
    @ColumnInfo(name = Battery.COLUMN_CURRENT_AVG_MA) val currentAvgMa: Float,
    @ColumnInfo(name = Battery.COLUMN_CHARGE_COUNTER_MAH) val chargeCounterMah: Int,
    @ColumnInfo(name = Battery.COLUMN_WATTS) val watts: Float,
    @ColumnInfo(name = Battery.COLUMN_IS_SCREEN_ON) val isScreenOn: Int,
    @ColumnInfo(name = Battery.COLUMN_IS_BATTERY_SAVER) val isBatterySaver: Int,
    @ColumnInfo(name = Battery.COLUMN_IS_DOZE_MODE) val isDozeMode: Int,
    @ColumnInfo(name = Battery.COLUMN_CYCLE_COUNT) val cycleCount: Int,
    @ColumnInfo(name = Battery.COLUMN_CHARGE_TIME_REMAINING_MS) val chargeTimeRemainingMs: Long,
    @ColumnInfo(name = Battery.COLUMN_FULL_CAPACITY_MAH) val fullCapacityMah: Int,
    @ColumnInfo(name = Battery.COLUMN_DESIGN_CAPACITY_MAH) val designCapacityMah: Int,
)