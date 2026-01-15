package com.habitiora.batty.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.habitiora.batty.navigation.MainNavHost
import com.habitiora.batty.navigation.MainNavigationBar
import com.habitiora.batty.navigation.MainTopBar

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { MainTopBar(navController) },
        bottomBar = { MainNavigationBar(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)){
            MainNavHost(navController)
        }
    }
}
