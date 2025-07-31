package com.habitiora.batty.data.database.converters

import androidx.room.TypeConverter
import com.habitiora.batty.domain.model.BatteryHealth
import com.habitiora.batty.domain.model.ChargingType

class BatteryTypeConverters {

    @TypeConverter
    fun fromChargingType(type: ChargingType): String = type.name

    @TypeConverter
    fun toChargingType(value: String): ChargingType =
        runCatching { ChargingType.valueOf(value) }.getOrDefault(ChargingType.UNKNOWN)

    @TypeConverter
    fun fromBatteryHealth(health: BatteryHealth): String = health.name

    @TypeConverter
    fun toBatteryHealth(value: String): BatteryHealth =
        runCatching { BatteryHealth.valueOf(value) }.getOrDefault(BatteryHealth.UNKNOWN)
}
