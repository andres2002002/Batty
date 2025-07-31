package com.habitiora.batty.data.converters

import com.habitiora.batty.data.entity.BatteryEntity
import com.habitiora.batty.domain.model.BatteryState

fun BatteryState.toBatteryEntity(): BatteryEntity {
    return BatteryEntity(
        id = this.id,
        timestamp = this.timestamp,
        batteryLevel = this.batteryLevel,
        isCharging = this.isCharging,
        chargingType = this.chargingType,
        temperature = this.temperature,
        voltage = this.voltage,
        current = this.current,
        capacity = this.capacity,
        screenOn = this.screenOn,
        powerSaveMode = this.powerSaveMode,
        batteryHealth = this.batteryHealth,
        chargingRate = this.chargingRate,
        dischargeRate = this.dischargeRate,
        estimatedTimeRemaining = this.estimatedTimeRemaining,
        sessionId = this.sessionId
    )
}

fun BatteryEntity.toBatteryState(): BatteryState {
    return BatteryState(
        id = this.id,
        timestamp = this.timestamp,
        batteryLevel = this.batteryLevel,
        isCharging = this.isCharging,
        chargingType = this.chargingType,
        temperature = this.temperature,
        voltage = this.voltage,
        current = this.current,
        capacity = this.capacity,
        screenOn = this.screenOn,
        powerSaveMode = this.powerSaveMode,
        batteryHealth = this.batteryHealth,
        chargingRate = this.chargingRate,
        dischargeRate = this.dischargeRate,
        estimatedTimeRemaining = this.estimatedTimeRemaining,
        sessionId = this.sessionId
    )
}