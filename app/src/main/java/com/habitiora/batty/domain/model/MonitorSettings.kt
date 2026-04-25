package com.habitiora.batty.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class MonitorSettings(
    val monitorBattery: Boolean = true,
    val alertPolicy: AlertPolicy = AlertPolicy.All,
    val startOnBoot: Boolean = false
)