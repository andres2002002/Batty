package com.habitiora.batty.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitiora.batty.navigation.Screens
import com.habitiora.batty.viewmodel.BatteryMainInfoVM

@Composable
fun BatteryMainInfo(viewModel: BatteryMainInfoVM = hiltViewModel()){
    val state by viewModel.batteryState.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Nivel: ${state?.level ?: "--"}%", style = MaterialTheme.typography.titleLarge)
        Text("Cargando: ${if (state?.isCharging == true) "Sí" else "No"}")
        Text("Temperatura: ${state?.temperature ?: "--"}°C")
        Text("Fuente: ${state?.chargingSource ?: "--"}")
    }
}