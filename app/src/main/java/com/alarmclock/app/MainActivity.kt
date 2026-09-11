package com.alarmclock.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.alarmclock.app.navigation.AppNavHost
import com.alarmclock.app.settings.LocaleHelper
import com.alarmclock.app.theme.AlarmClockTheme

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AlarmClockApp
        var ready = false
        splash.setKeepOnScreenCondition { !ready }

        setContent {
            val settings by app.settingsDataStore.settings.collectAsState(initial = null)
            ready = settings != null
            val currentSettings = settings
            if (currentSettings != null) {
                AlarmClockTheme(themeMode = currentSettings.themeMode) {
                    AppNavHost(startOnboarding = !currentSettings.onboardingComplete)
                }
            }
        }
    }
}
