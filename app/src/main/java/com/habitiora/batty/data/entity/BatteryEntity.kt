package com.habitiora.batty.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.habitiora.batty.domain.model.BatteryHealth
import com.habitiora.batty.domain.model.ChargingType

const val tableBatteryStateName = "battery_state_table"

@Entity(tableName = tableBatteryStateName)
data class BatteryEntity(
    // Datos básicos
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "battery_level") val batteryLevel: Int,
    @ColumnInfo(name = "is_charging") val isCharging: Boolean,
    @ColumnInfo(name = "charging_type") val chargingType: ChargingType,

    // Datos técnicos
    @ColumnInfo(name = "temperature") val temperature: Int,          // Temperatura en décimas de grado (250 = 25.0°C)
    @ColumnInfo(name = "voltage") val voltage: Int,              // Voltaje en mV
    @ColumnInfo(name = "current") val current: Int?,             // Corriente en mA (si está disponible)
    @ColumnInfo(name = "capacity") val capacity: Long?,           // Capacidad restante en µAh

    // Contexto del sistema
    @ColumnInfo(name = "screen_on") val screenOn: Boolean,         // ¿Pantalla encendida?
    @ColumnInfo(name = "power_save_mode") val powerSaveMode: Boolean,    // ¿Modo ahorro energía activo?
    @ColumnInfo(name = "battery_health") val batteryHealth: BatteryHealth, // Asegúrate de tener un TypeConverter si BatteryHealth no es un tipo primitivo o parcelable

    // Estadísticas calculadas
    @ColumnInfo(name = "charging_rate") val chargingRate: Float?,      // %/hora (calculado)
    @ColumnInfo(name = "discharge_rate") val dischargeRate: Float?,     // %/hora (calculado)
    @ColumnInfo(name = "estimated_time_remaining") val estimatedTimeRemaining: Long?, // Minutos estimados

    // Metadatos
    @ColumnInfo(name = "session_id") val sessionId: String,         // ID de sesión de carga/descarga
)