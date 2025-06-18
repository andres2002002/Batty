package com.habitiora.batty.viewmodel

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import com.habitiora.batty.domain.BatteryState
import com.habitiora.batty.services.BatteryReceiver
import com.habitiora.batty.services.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BatteryMainInfoVM @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _batteryState = MutableStateFlow<BatteryState?>(null)
    val batteryState: StateFlow<BatteryState?> = _batteryState.asStateFlow()

    private fun updateBatteryState(newState: BatteryState) {
        _batteryState.value = newState
    }

    private val batteryReceiver = BatteryReceiver { state ->
        updateBatteryState(state)
        Timber.d("Battery state: $state")
    }
    init {
        context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }
}