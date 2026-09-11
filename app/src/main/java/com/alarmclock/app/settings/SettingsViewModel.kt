package com.alarmclock.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmclock.app.alarm.AlarmScheduler
import com.alarmclock.app.data.settings.AppSettings
import com.alarmclock.app.data.settings.SettingsDataStore
import com.alarmclock.app.theme.AppThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val dataStore: SettingsDataStore,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val settings: StateFlow<AppSettings> = dataStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun cycleTheme() {
        viewModelScope.launch {
            val current = settings.value.themeMode
            val next = when (current) {
                AppThemeMode.AUTO -> AppThemeMode.LIGHT
                AppThemeMode.LIGHT -> AppThemeMode.DARK
                AppThemeMode.DARK -> AppThemeMode.AUTO
            }
            dataStore.setThemeMode(next)
        }
    }

    fun setLanguage(code: String, name: String) {
        viewModelScope.launch { dataStore.setLanguage(code, name) }
    }

    fun setVibration(enabled: Boolean) {
        viewModelScope.launch { dataStore.setVibrationEnabled(enabled) }
    }

    fun setUpcomingNotification(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setUpcomingAlarmNotification(enabled)
            alarmScheduler.refreshUpcomingNotification()
        }
    }

    fun setLocationPermission(enabled: Boolean) {
        viewModelScope.launch { dataStore.setLocationPermissionEnabled(enabled) }
    }
}
