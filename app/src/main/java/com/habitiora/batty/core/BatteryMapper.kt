package com.habitiora.batty.core

import com.habitiora.batty.data.local.room.entity.BatteryEntity
import com.habitiora.batty.data.local.room.pojo.BatteryDataPointEntity
import com.habitiora.batty.data.local.room.pojo.BatteryStatsSummary
import com.habitiora.batty.domain.model.BatteryDataPoint
import com.habitiora.batty.domain.model.BatteryHealth
import com.habitiora.batty.domain.model.BatteryInfo
import com.habitiora.batty.domain.model.BatteryPlugged
import com.habitiora.batty.domain.model.BatteryStats
import com.habitiora.batty.domain.model.BatteryStatus


fun BatteryInfo.toEntity(): BatteryEntity = BatteryEntity(
    id = id,
    level = level,
    status = status.name,
    health = health.name,
    plugged = plugged.name,
    temperature = temperature,
    voltage = voltage,
    technology = technology,
    timestamp = timestamp,
    currentNowMa = currentNowMa,
    currentAvgMa = currentAvgMa,
    chargeCounterMah = chargeCounterMah,
    watts = watts,
    isScreenOn = if (isScreenOn) 1 else 0,
    isBatterySaver = if (isBatterySaver) 1 else 0,
    isDozeMode = if (isDozeMode) 1 else 0,
    cycleCount = cycleCount,
    chargeTimeRemainingMs = chargeTimeRemainingMs,
    fullCapacityMah = fullCapacityMah,
    designCapacityMah = designCapacityMah,
)

fun BatteryEntity.toDomain(): BatteryInfo = BatteryInfo(
    id = id,
    level = level,
    status = runCatching { BatteryStatus.valueOf(status) }.getOrDefault(BatteryStatus.UNKNOWN),
    health = runCatching { BatteryHealth.valueOf(health) }.getOrDefault(BatteryHealth.UNKNOWN),
    plugged = runCatching { BatteryPlugged.valueOf(plugged) }.getOrDefault(BatteryPlugged.NONE),
    temperature = temperature,
    voltage = voltage,
    technology = technology,
    timestamp = timestamp,
    currentNowMa = currentNowMa,
    currentAvgMa = currentAvgMa,
    chargeCounterMah = chargeCounterMah,
    watts = watts,
    isScreenOn = isScreenOn != 0,
    isBatterySaver = isBatterySaver != 0,
    isDozeMode = isDozeMode != 0,
    cycleCount = cycleCount,
    chargeTimeRemainingMs = chargeTimeRemainingMs,
    fullCapacityMah = fullCapacityMah,
    designCapacityMah = designCapacityMah,
)

fun BatteryStatsSummary.toDomain(): BatteryStats = BatteryStats(
    avgLevel = avgLevel ?: 0f,
    minLevel = minLevel ?: 0,
    maxLevel = maxLevel ?: 0,
    avgTemperature = avgTemperature ?: 0f,
    minTemperature = minTemperature ?: 0f,
    maxTemperature = maxTemperature ?: 0f,
    avgVoltage = avgVoltage ?: 0f,
    avgCurrentMa = avgCurrentMa ?: 0f,
    peakCurrentMa = peakCurrentMa ?: 0f,
    avgWatts = avgWatts ?: 0f,
    peakWatts = peakWatts ?: 0f,
    screenOnSamples = screenOnSamples,
    batterySaverSamples = batterySaverSamples,
    totalSamples = totalSamples,
)


fun BatteryDataPointEntity.toDomain(): BatteryDataPoint = BatteryDataPoint(
    timestamp   = timestamp,
    level       = level,
    temperature = temperature,
    currentMa   = currentNowMa,
    watts       = watts,
)