package com.habitiora.batty.domain.useCase

import com.habitiora.batty.domain.model.BatteryDataPoint
import com.habitiora.batty.domain.model.LastCycleStats
import javax.inject.Inject

class GetLastChargingCycleUseCase @Inject constructor() {

    operator fun invoke(data: List<BatteryDataPoint>): LastCycleStats? {
        if (data.isEmpty()) return null

        var connectedAt: Long? = null
        var disconnectedAt: Long? = null
        var startLevel = 0
        var endLevel = 0

        // Recorremos de atrás hacia adelante para encontrar el último evento de carga
        for (i in data.indices.reversed()) {
            val point = data[i]
            val prevPoint = data.getOrNull(i - 1)

            if (point.isCharging && disconnectedAt == null && (prevPoint == null || !prevPoint.isCharging)) {
                // Sigue conectado actualmente (comenzó a cargar aquí)
                connectedAt = point.timestamp
                startLevel = point.level
                endLevel = data.last().level
                break
            }

            if (!point.isCharging && prevPoint?.isCharging == true && disconnectedAt == null) {
                // Punto exacto de desconexión
                disconnectedAt = point.timestamp
                endLevel = prevPoint.level
            }

            if (point.isCharging && disconnectedAt != null && prevPoint?.isCharging == false) {
                // Punto exacto de conexión de ese ciclo finalizado
                connectedAt = point.timestamp
                startLevel = point.level
                break
            }
        }

        if (connectedAt == null) return null

        val endTime = disconnectedAt ?: data.last().timestamp

        return LastCycleStats(
            connectedAt = connectedAt,
            disconnectedAt = disconnectedAt,
            durationConnectedMs = endTime - connectedAt,
            levelGained = (endLevel - startLevel).coerceAtLeast(0)
        )
    }
}