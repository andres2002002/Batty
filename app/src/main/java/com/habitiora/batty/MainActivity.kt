package com.habitiora.batty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.habitiora.batty.services.BatteryForegroundService
import com.habitiora.batty.services.PermissionsRequesterFactory
import com.habitiora.batty.ui.screens.MainScaffold
import com.habitiora.batty.ui.theme.BattyTheme
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var permissionsRequesterFactory: PermissionsRequesterFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionsRequester = permissionsRequesterFactory.create(this)
        permissionsRequester.requestNotificationAccess()
        permissionsRequester.requestDndAccess()

        startBatteryMonitorService(this)

        enableEdgeToEdge()
        setContent {
            BattyTheme {
                MainScaffold()
            }
        }
    }

    fun startBatteryMonitorService(context: Context) {
        val serviceIntent = Intent(context, BatteryForegroundService::class.java)
        context.startForegroundService(serviceIntent)
    }
}