package com.alarmclock.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.alarmclock.app.alarm.AddEditAlarmScreen
import com.alarmclock.app.alarm.AlarmListScreen
import com.alarmclock.app.alarm.AlarmSearchScreen
import com.alarmclock.app.common.components.AppBottomNavBar
import com.alarmclock.app.common.util.currentApp
import com.alarmclock.app.onboarding.ConsentScreen
import com.alarmclock.app.onboarding.PermissionsScreen
import com.alarmclock.app.settings.ChangeLanguageScreen
import com.alarmclock.app.settings.PrivacySettingsScreen
import com.alarmclock.app.settings.SettingsScreen
import com.alarmclock.app.stopwatch.StopwatchScreen
import com.alarmclock.app.timer.TimerScreen
import com.alarmclock.app.worldclock.AddCityScreen
import com.alarmclock.app.worldclock.WorldClockScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(startOnboarding: Boolean) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val app = currentApp()
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            if (currentRoute in Destinations.bottomNavRoutes) {
                AppBottomNavBar(currentRoute = currentRoute) { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { outerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (startOnboarding) Destinations.PERMISSIONS else Destinations.ALARM_LIST,
            modifier = Modifier.padding(bottom = outerPadding.calculateBottomPadding())
        ) {
            composable(Destinations.PERMISSIONS) {
                PermissionsScreen(onContinue = {
                    navController.navigate(Destinations.CONSENT) {
                        popUpTo(Destinations.PERMISSIONS) { inclusive = true }
                    }
                })
            }
            composable(Destinations.CONSENT) {
                ConsentScreen(onDone = {
                    scope.launch { app.settingsDataStore.setOnboardingComplete(true) }
                    navController.navigate(Destinations.ALARM_LIST) {
                        popUpTo(Destinations.CONSENT) { inclusive = true }
                    }
                })
            }

            composable(Destinations.ALARM_LIST) {
                AlarmListScreen(
                    onAddAlarm = { navController.navigate(Destinations.addEditAlarmRoute()) },
                    onEditAlarm = { id -> navController.navigate(Destinations.addEditAlarmRoute(id)) },
                    onSearch = { navController.navigate(Destinations.ALARM_SEARCH) },
                    onSettings = { navController.navigate(Destinations.SETTINGS) }
                )
            }
            composable(
                Destinations.ADD_EDIT_ALARM_ROUTE,
                arguments = listOf(navArgument(Destinations.ADD_EDIT_ALARM_ARG) {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { entry ->
                val id = entry.arguments?.getLong(Destinations.ADD_EDIT_ALARM_ARG) ?: -1L
                AddEditAlarmScreen(
                    alarmId = if (id < 0) null else id,
                    onDone = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Destinations.ALARM_SEARCH) {
                AlarmSearchScreen(
                    onBack = { navController.popBackStack() },
                    onEditAlarm = { id -> navController.navigate(Destinations.addEditAlarmRoute(id)) }
                )
            }

            composable(Destinations.WORLD_CLOCK) {
                WorldClockScreen(
                    onAddCity = { navController.navigate(Destinations.ADD_CITY) },
                    onSettings = { navController.navigate(Destinations.SETTINGS) }
                )
            }
            composable(Destinations.ADD_CITY) {
                AddCityScreen(
                    onBack = { navController.popBackStack() },
                    onCityAdded = { navController.popBackStack() }
                )
            }

            composable(Destinations.STOPWATCH) {
                StopwatchScreen(onSettings = { navController.navigate(Destinations.SETTINGS) })
            }

            composable(Destinations.TIMER) {
                TimerScreen(onSettings = { navController.navigate(Destinations.SETTINGS) })
            }

            composable(Destinations.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onChangeLanguage = { navController.navigate(Destinations.CHANGE_LANGUAGE) },
                    onPrivacySettings = { navController.navigate(Destinations.PRIVACY_SETTINGS) }
                )
            }
            composable(Destinations.CHANGE_LANGUAGE) {
                ChangeLanguageScreen(onBack = { navController.popBackStack() })
            }
            composable(Destinations.PRIVACY_SETTINGS) {
                PrivacySettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
