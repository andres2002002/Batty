package com.habitiora.batty.services

import android.content.Context
import com.habitiora.batty.data.manager.BatteryTimeEstimator
import com.habitiora.batty.domain.model.BatteryState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

interface BatteryStateProvider {
    fun observeBatteryState(): Flow<BatteryState>
}

class BatteryStateProviderImp @Inject constructor(
    @ApplicationContext private val context: Context,
    private val estimator: BatteryTimeEstimator
) : BatteryStateProvider {


    override fun observeBatteryState(): Flow<BatteryState> = callbackFlow {

    }
}
