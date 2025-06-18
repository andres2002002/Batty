package com.habitiora.batty.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.habitiora.batty.ui.screens.BatteryMainInfo

@Composable
fun MainNavHost(
    navController: NavHostController,
){
    NavHost(
        navController = navController,
        startDestination = Screens.BatteryMainInfo.route
    ) {
        composable(Screens.BatteryMainInfo.route) {
            BatteryMainInfo()
        }
        composable(Screens.History.route) {
        }
        composable(Screens.Settings.route) {
        }
        composable(Screens.Info.route) {
        }
    }
}

@Composable
fun MainNavigationBar(
    navController: NavHostController,
){
    var selectedItem: String by rememberSaveable(navController.currentDestination) {
        mutableStateOf(navController.currentDestination?.route?: Screens.BatteryMainInfo.route)
    }
    val items = mapOf(
        "Home" to Screens.BatteryMainInfo,
        "History" to Screens.History,
        "Settings" to Screens.Settings,
        "Info" to Screens.Info,
    )
    Icons.Filled.Settings

    NavigationBar {
        items.forEach { item ->
            val selected = selectedItem == item.value.route
            val icon = if (selected) item.value.activeIconRes else item.value.inactiveIconRes
            NavigationBarItem(
                selected = selected,
                icon = { Icon(ImageVector.vectorResource(icon), contentDescription = item.key) },
                label = { Text(item.key) },
                onClick = {
                    selectedItem = item.value.route
                    navController.navigate(item.value.route)
                }
            )
        }
    }
}