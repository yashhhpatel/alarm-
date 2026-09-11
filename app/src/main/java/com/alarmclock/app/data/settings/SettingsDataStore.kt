package com.alarmclock.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alarmclock.app.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.AUTO,
    val languageCode: String = "en",
    val languageName: String = "English",
    val vibrationEnabled: Boolean = true,
    val upcomingAlarmNotification: Boolean = false,
    val onboardingComplete: Boolean = false,
    val locationPermissionEnabled: Boolean = true
)

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val LANG_CODE = stringPreferencesKey("language_code")
        val LANG_NAME = stringPreferencesKey("language_name")
        val VIBRATION = booleanPreferencesKey("vibration_enabled")
        val UPCOMING_NOTIF = booleanPreferencesKey("upcoming_alarm_notification")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_complete")
        val LOCATION_PERMISSION = booleanPreferencesKey("location_permission_enabled")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[Keys.THEME]?.let { runCatching { AppThemeMode.valueOf(it) }.getOrNull() }
                ?: AppThemeMode.AUTO,
            languageCode = prefs[Keys.LANG_CODE] ?: "en",
            languageName = prefs[Keys.LANG_NAME] ?: "English",
            vibrationEnabled = prefs[Keys.VIBRATION] ?: true,
            upcomingAlarmNotification = prefs[Keys.UPCOMING_NOTIF] ?: false,
            onboardingComplete = prefs[Keys.ONBOARDING_DONE] ?: false,
            locationPermissionEnabled = prefs[Keys.LOCATION_PERMISSION] ?: true
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setLanguage(code: String, name: String) {
        context.dataStore.edit {
            it[Keys.LANG_CODE] = code
            it[Keys.LANG_NAME] = name
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VIBRATION] = enabled }
    }

    suspend fun setUpcomingAlarmNotification(enabled: Boolean) {
        context.dataStore.edit { it[Keys.UPCOMING_NOTIF] = enabled }
    }

    suspend fun setOnboardingComplete(done: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    suspend fun setLocationPermissionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LOCATION_PERMISSION] = enabled }
    }
}
