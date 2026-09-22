package com.yarom.jewishcalendar

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yarom.jewishcalendar.data.repository.ThemeMode
import com.yarom.jewishcalendar.ui.CalendarViewModel
import com.yarom.jewishcalendar.ui.EventViewModel
import com.yarom.jewishcalendar.ui.SettingsViewModel
import com.yarom.jewishcalendar.ui.appViewModelFactory
import com.yarom.jewishcalendar.ui.navigation.Screen
import com.yarom.jewishcalendar.ui.navigation.bottomNavItems
import com.yarom.jewishcalendar.ui.screens.daily.DailyScreen
import com.yarom.jewishcalendar.ui.screens.monthly.MonthlyScreen
import com.yarom.jewishcalendar.ui.screens.settings.SettingsScreen
import com.yarom.jewishcalendar.ui.screens.weekly.WeeklyScreen
import com.yarom.jewishcalendar.ui.theme.JewishCalendarTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TEMPORARY diagnostic: read back any crash JewishCalendarApp's handler persisted from
        // the previous run, since this device has no adb/logcat access. Remove this block (and
        // the handler in JewishCalendarApp) once the root cause is confirmed fixed.
        val crashPrefs = getSharedPreferences("crash_report", MODE_PRIVATE)
        val lastCrash = crashPrefs.getString("last_crash", null)
        if (lastCrash != null) {
            setContent { CrashReportScreen(lastCrash) { crashPrefs.edit().clear().apply(); recreate() } }
            return
        }

        val app = application as JewishCalendarApp
        val factory = appViewModelFactory(app)

        setContent {
            val calendarViewModel: CalendarViewModel = viewModel(factory = factory)
            val eventViewModel: EventViewModel = viewModel(factory = factory)
            val settingsViewModel: SettingsViewModel = viewModel(factory = factory)

            val settings by settingsViewModel.settings.collectAsState()

            RequestLocationPermission(onGranted = { settingsViewModel.useDeviceLocation() })

            JewishCalendarTheme(
                darkTheme = when (settings?.themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    else -> androidx.compose.foundation.isSystemInDarkTheme()
                },
                dynamicColor = settings?.useDynamicColor ?: false,
            ) {
                CalendarApp(calendarViewModel, eventViewModel, settingsViewModel)
            }
        }
    }
}

@Composable
private fun CrashReportScreen(trace: String, onDismiss: () -> Unit) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("האפליקציה קרסה בהפעלה הקודמת", style = MaterialTheme.typography.titleLarge)
                Text("אפשר להעתיק/לצלם מסך של הטקסט הבא ולשלוח:")
                SelectionContainer {
                    Text(trace, style = MaterialTheme.typography.bodySmall)
                }
                Button(onClick = onDismiss) { Text("נקה והמשך לאפליקציה") }
            }
        }
    }
}

@Composable
private fun RequestLocationPermission(onGranted: () -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        if (results.values.any { it }) onGranted()
    }

    LaunchedEffect(Unit) {
        launcher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
        )
    }
}

@Composable
private fun CalendarApp(
    calendarViewModel: CalendarViewModel,
    eventViewModel: EventViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = com.yarom.jewishcalendar.ui.theme.DeskBackground,
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.screen.route,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(item.labelRes)) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Weekly.route,
            modifier = Modifier
                .padding(padding)
                .padding(10.dp),
        ) {
            composable(Screen.Weekly.route) {
                WeeklyScreen(
                    calendarViewModel = calendarViewModel,
                    eventViewModel = eventViewModel,
                    onDayOpened = {
                        navController.navigate(Screen.Daily.route) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(Screen.Daily.route) { DailyScreen(calendarViewModel, eventViewModel) }
            composable(Screen.Monthly.route) { MonthlyScreen(calendarViewModel, eventViewModel) }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onManageEvents = { navController.navigate(Screen.Events.route) },
                )
            }
            composable(Screen.Events.route) {
                com.yarom.jewishcalendar.ui.screens.events.EventsManagementScreen(
                    eventViewModel = eventViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
