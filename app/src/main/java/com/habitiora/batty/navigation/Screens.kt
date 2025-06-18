package com.habitiora.batty.navigation

import androidx.annotation.DrawableRes
import com.habitiora.batty.R

sealed class Screens(
    val route: String,
    @DrawableRes val activeIconRes: Int,
    @DrawableRes val inactiveIconRes: Int = activeIconRes
){
    data object BatteryMainInfo: Screens("battery_main_info", R.drawable.rounded_battery_horiz_050_24, R.drawable.rounded_battery_horiz_000_24)
    data object History: Screens("history", R.drawable.rounded_area_chart_24, R.drawable.outline_area_chart_24)
    data object Settings: Screens("settings", R.drawable.baseline_build_24, R.drawable.baseline_build_24)
    data object Info: Screens("info", R.drawable.baseline_info_24, R.drawable.outline_info_24)
}