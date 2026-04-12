package com.habitiora.batty.data.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.habitiora.batty.domain.controller.BatteryServiceController
import com.habitiora.batty.domain.model.ConnectionState
import com.habitiora.batty.services.BatteryMonitorService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryServiceControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BatteryServiceController {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Timber.d("ServiceConnection → connected: $name")
            _connectionState.value = ConnectionState.Connected
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Timber.w("ServiceConnection → unexpected disconnect: $name")
            isBound = false
            _connectionState.value = ConnectionState.Disconnected
        }

        override fun onBindingDied(name: ComponentName) {
            Timber.w("ServiceConnection → binding died: $name")
            isBound = false
            _connectionState.value = ConnectionState.Disconnected
        }

        override fun onNullBinding(name: ComponentName) {
            Timber.e("ServiceConnection → null binding: $name")
            _connectionState.value = ConnectionState.Unavailable
        }
    }

    override fun checkCurrentBinding() {
        if (isBound) return
        _connectionState.value = ConnectionState.Connecting

        val intent = Intent(context, BatteryMonitorService::class.java)
        val bound = context.bindService(intent, serviceConnection, 0)

        if (!bound) {
            Timber.d("ServiceConnection → bind returned false (service not running)")
            _connectionState.value = ConnectionState.Unavailable
        } else {
            isBound = true
            Timber.d("ServiceConnection → binding requested")
        }
    }

    override fun startServiceAndBind() {
        runCatching {
            val intent = BatteryMonitorService.startIntent(context)
            context.startForegroundService(intent)
            bindWithAutoCreate()
        }.onFailure { Timber.e(it, "Error starting and binding service") }
    }

    override fun stopServiceAndUnbind() {
        runCatching {
            context.startService(BatteryMonitorService.stopIntent(context))
            unbindOnly()
        }.onFailure { Timber.e(it, "Error stopping service") }
    }

    override fun unbindOnly() {
        if (!isBound) return
        runCatching { context.unbindService(serviceConnection) }
            .onFailure { Timber.w(it, "ServiceConnection → unbind failed") }
        isBound = false
        _connectionState.value = ConnectionState.Idle
        Timber.d("ServiceConnection → unbound")
    }

    private fun bindWithAutoCreate() {
        if (isBound) return
        _connectionState.value = ConnectionState.Connecting

        val intent = Intent(context, BatteryMonitorService::class.java)
        val bound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        if (!bound) {
            _connectionState.value = ConnectionState.Unavailable
            Timber.e("ServiceConnection → bindAfterStart failed")
        } else {
            isBound = true
        }
    }
}