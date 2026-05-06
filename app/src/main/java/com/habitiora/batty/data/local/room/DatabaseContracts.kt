package com.habitiora.batty.data.local.room

object DatabaseContracts {
    const val DATABASE_NAME = "battery_tracker.db"
    object Battery{
        const val TABLE_NAME = "battery_snapshots"
        const val COLUMN_ID = "id"
        const val COLUMN_LEVEL = "level"
        const val COLUMN_STATUS = "status"
        const val COLUMN_HEALTH = "health"
        const val COLUMN_PLUGGED = "plugged"
        const val COLUMN_TEMPERATURE = "temperature"
        const val COLUMN_VOLTAGE = "voltage"
        const val COLUMN_TECHNOLOGY = "technology"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_CURRENT_NOW_MA = "current_now_ma"
        const val COLUMN_CURRENT_AVG_MA = "current_avg_ma"
        const val COLUMN_CHARGE_COUNTER_MAH = "charge_counter_mah"
        const val COLUMN_WATTS = "watts"
        const val COLUMN_IS_SCREEN_ON = "is_screen_on"
        const val COLUMN_IS_BATTERY_SAVER = "is_battery_saver"
        const val COLUMN_IS_DOZE_MODE = "is_doze_mode"
        const val COLUMN_CYCLE_COUNT = "cycle_count"
        const val COLUMN_CHARGE_TIME_REMAINING_MS = "charge_time_remaining_ms"
        const val COLUMN_FULL_CAPACITY_MAH = "full_capacity_mah"
        const val COLUMN_DESIGN_CAPACITY_MAH = "design_capacity_mah"
    }
}