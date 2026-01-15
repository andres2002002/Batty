package com.habitiora.batty.domain.model

/**
 * Clase que representa el estado de la batería.
 * @property id Identificador único de la batería.
 * @property timestamp Marca de tiempo en milisegundos.
 * @property batteryLevel Nivel de batería en porcentaje.
 * @property isCharging Indica si la batería está cargando.
 * @property chargingType Tipo de carga actual.
 * @property temperature Temperatura de la batería en décimas de grado (250 = 25.0°C).
 * @property voltage Voltaje de la batería en mV.
 * @property current Corriente de la batería en µA.
 * @property capacity Capacidad restante de la batería en µAh.
 * @property screenOn Indica si la pantalla está encendida.
 * @property powerSaveMode Indica si el modo ahorro energía está activo.
 * @property batteryHealth Estado de salud de la batería.
 * @property chargingRate Tasa de carga en %.
 * @property dischargeRate Tasa de descarga en %.
 * @property estimatedTimeRemaining Tiempo estimado de descarga en minutos.
 * @property sessionId ID de sesión de carga/descarga.
 */
data class BatteryState(
    // Datos básicos
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val batteryLevel: Int = -1,
    val isCharging: Boolean = false,
    val chargingType: ChargingType = ChargingType.UNKNOWN,

    // Datos técnicos
    val temperature: Int = -1,          // Temperatura en décimas de grado (250 = 25.0°C)
    val voltage: Int = -1,              // Voltaje en mV
    val current: Int? = null,             // Corriente en µA
    val capacity: Long? = null,           // Capacidad restante en µAh

    // Contexto del sistema
    val screenOn: Boolean = false,         // ¿Pantalla encendida?
    val powerSaveMode: Boolean = false,    // ¿Modo ahorro energía activo?
    val batteryHealth: BatteryHealth = BatteryHealth.UNKNOWN,

    // Estadísticas calculadas
    val chargingRate: Float? = null,      // %/hora (calculado)
    val dischargeRate: Float? = null,     // %/hora (calculado)
    val estimatedTimeRemaining: Long? = null, // Minutos estimados

    // Metadatos
    val sessionId: String = "",         // ID de sesión de carga/descarga
)