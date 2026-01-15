package com.habitiora.batty.data.manager

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class BatteryTimeEstimator @Inject constructor(
    private val maxSamples: Int = 10, // Increased for better accuracy
    private val minSamplesForEstimate: Int = 3, // Minimum samples needed
    private val maxSampleAgeMs: Long = 10 * 60 * 1000L // 10 minutes max age
) {
    private val samples = ArrayDeque<BatterySample>()

    data class BatterySample(
        val level: Int,
        val timestamp: Long,
        val isCharging: Boolean
    )

    /**
     * Records a new battery level sample.
     */
    fun recordSample(
        level: Int,
        timestamp: Long = System.currentTimeMillis(),
        isCharging: Boolean = true
    ) {
        // Remove old samples
        removeOldSamples(timestamp)

        // Add new sample
        samples.addLast(BatterySample(level, timestamp, isCharging))

        // Keep only recent samples
        while (samples.size > maxSamples) {
            samples.removeFirst()
        }
    }

    /**
     * Estimates time to full charge (in milliseconds).
     */
    fun estimateTimeToFullMillis(): Long? {
        val chargingSamples = samples.filter { it.isCharging }
        if (chargingSamples.size < minSamplesForEstimate) return null

        val rate = calculateRateFromSamples(chargingSamples) ?: return null
        if (rate <= 0) return null

        val currentLevel = chargingSamples.lastOrNull()?.level ?: return null
        val remainingPercent = 100 - currentLevel

        return (remainingPercent / rate).toLong()
    }

    /**
     * Estimates time to empty (in milliseconds).
     */
    fun estimateTimeToEmptyMillis(): Long? {
        val dischargingSamples = samples.filter { !it.isCharging }
        if (dischargingSamples.size < minSamplesForEstimate) return null

        val rate = calculateRateFromSamples(dischargingSamples) ?: return null
        if (rate >= 0) return null // Should be negative for discharging

        val currentLevel = dischargingSamples.lastOrNull()?.level ?: return null

        return (currentLevel / abs(rate)).toLong()
    }

    /**
     * Estimates the charging/discharging rate in % per hour.
     */
    fun estimateRatePerHour(): Float? {
        if (samples.size < minSamplesForEstimate) return null

        return calculateRateFromSamples(samples.toList())?.let { ratePerMs ->
            (ratePerMs * 3600000).toFloat() // Convert to per hour
        }
    }

    /**
     * Gets the current trend (charging, discharging, stable).
     */
    fun getBatteryTrend(): BatteryTrend {
        val rate = estimateRatePerHour() ?: return BatteryTrend.UNKNOWN

        return when {
            rate > 0.5 -> BatteryTrend.CHARGING
            rate < -0.5 -> BatteryTrend.DISCHARGING
            else -> BatteryTrend.STABLE
        }
    }

    /**
     * Gets charging efficiency based on recent samples.
     */
    fun getChargingEfficiency(): Float? {
        val chargingSamples = samples.filter { it.isCharging }
        if (chargingSamples.size < 2) return null

        val rate = calculateRateFromSamples(chargingSamples) ?: return null

        // Normalize efficiency (this is a simplified calculation)
        // In reality, you'd compare against expected charging rates
        return when {
            rate > 0.8 -> 1.0f // Excellent
            rate > 0.5 -> 0.8f // Good
            rate > 0.2 -> 0.6f // Fair
            else -> 0.3f // Poor
        }
    }

    private fun removeOldSamples(currentTime: Long) {
        while (samples.isNotEmpty() &&
            (currentTime - samples.first().timestamp) > maxSampleAgeMs) {
            samples.removeFirst()
        }
    }

    private fun calculateRateFromSamples(sampleList: List<BatterySample>): Double? {
        if (sampleList.size < 2) return null

        // Use linear regression for better accuracy
        val n = sampleList.size
        var sumX = 0.0 // time
        var sumY = 0.0 // level
        var sumXY = 0.0
        var sumXX = 0.0

        val baseTime = sampleList.first().timestamp

        sampleList.forEach { sample ->
            val x = (sample.timestamp - baseTime).toDouble()
            val y = sample.level.toDouble()

            sumX += x
            sumY += y
            sumXY += x * y
            sumXX += x * x
        }

        val denominator = n * sumXX - sumX * sumX
        if (abs(denominator) < 1e-10) return null

        // Slope represents rate of change (% per millisecond)
        val slope = (n * sumXY - sumX * sumY) / denominator

        return slope
    }

    enum class BatteryTrend {
        CHARGING,
        DISCHARGING,
        STABLE,
        UNKNOWN
    }

    /**
     * Clears all samples (useful when starting a new session).
     */
    fun clearSamples() {
        samples.clear()
    }

    /**
     * Gets the number of samples currently stored.
     */
    fun getSampleCount(): Int = samples.size

    /**
     * Checks if there are enough samples for reliable estimates.
     */
    fun hasReliableData(): Boolean = samples.size >= minSamplesForEstimate
}