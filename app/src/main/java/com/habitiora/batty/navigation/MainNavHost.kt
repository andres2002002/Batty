package com.habitiora.batty.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.habitiora.batty.R
import com.habitiora.batty.ui.screens.main.BatteryMainInfo
import com.habitiora.batty.ui.screens.history.BatteryHistoryScreen
import com.habitiora.batty.ui.screens.info.InfoScreen
import com.habitiora.batty.ui.screens.settings.SettingsScreen

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
            BatteryHistoryScreen()
        }
        composable(Screens.Settings.route) {
            SettingsScreen()
        }
        composable(Screens.Info.route) {
            InfoScreen()
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
        R.string.menu_home to Screens.BatteryMainInfo,
        R.string.menu_history to Screens.History,
        R.string.menu_info to Screens.Info,
    )

    NavigationBar {
        items.forEach { item ->
            val selected = selectedItem == item.value.route
            val icon = if (selected) item.value.activeIconRes else item.value.inactiveIconRes
            NavigationBarItem(
                selected = selected,
                icon = { Icon(ImageVector.vectorResource(icon), contentDescription = stringResource(item.key)) },
                label = { Text(stringResource(item.key)) },
                onClick = {
                    if (navController.currentDestination?.route == item.value.route) return@NavigationBarItem
                    selectedItem = item.value.route
                    navController.navigate(item.value.route)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    navController: NavHostController,
){
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        actions = {
            IconButton(
                onClick = {
                    if (navController.currentDestination?.route == Screens.Settings.route)
                        navController.navigateUp()
                    else
                        navController.navigate(Screens.Settings.route)
                }
            ) {
                Icon(Icons.Filled.Settings, stringResource(R.string.menu_settings))
            }
        }
    )
}