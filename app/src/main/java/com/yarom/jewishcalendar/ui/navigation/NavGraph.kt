package com.yarom.jewishcalendar.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector
import com.yarom.jewishcalendar.R

sealed class Screen(val route: String) {
    data object Weekly : Screen("weekly")
    data object Daily : Screen("daily")
    data object Monthly : Screen("monthly")
    data object Settings : Screen("settings")
}

data class BottomNavItem(val screen: Screen, @StringRes val labelRes: Int, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Weekly, R.string.nav_weekly, Icons.Default.CalendarViewWeek),
    BottomNavItem(Screen.Daily, R.string.nav_daily, Icons.Default.Today),
    BottomNavItem(Screen.Monthly, R.string.nav_monthly, Icons.Default.CalendarViewMonth),
    BottomNavItem(Screen.Settings, R.string.nav_settings, Icons.Default.Settings),
)
