package com.habitiora.batty.domain.controller

import com.habitiora.batty.domain.model.ConnectionState
import kotlinx.coroutines.flow.StateFlow

interface BatteryServiceController {
    val connectionState: StateFlow<ConnectionState>

    fun checkCurrentBinding()
    fun startServiceAndBind()
    fun stopServiceAndUnbind()
    fun unbindOnly()
}