package com.alarmclock.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.alarmclock.app.data.local.AppDatabase
import com.alarmclock.app.data.repository.AlarmRepository
import com.alarmclock.app.data.repository.TimerRepository
import com.alarmclock.app.data.repository.WorldClockRepository
import com.alarmclock.app.data.settings.SettingsDataStore
import com.alarmclock.app.settings.LocaleHelper

class AlarmClockApp : Application() {

    lateinit var settingsDataStore: SettingsDataStore
        private set
    lateinit var alarmRepository: AlarmRepository
        private set
    lateinit var worldClockRepository: WorldClockRepository
        private set
    lateinit var timerRepository: TimerRepository
        private set

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.wrapContext(base))
    }

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        settingsDataStore = SettingsDataStore(this)
        alarmRepository = AlarmRepository(db.alarmDao())
        worldClockRepository = WorldClockRepository(db.worldClockDao())
        timerRepository = TimerRepository(db.timerPresetDao(), db.activeTimerDao())
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ALARM,
                getString(R.string.channel_alarm_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_alarm_desc)
                setBypassDnd(true)
                enableVibration(true)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_TIMER,
                getString(R.string.channel_timer_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_timer_desc)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_UPCOMING,
                getString(R.string.channel_upcoming_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_upcoming_desc)
            }
        )
    }

    companion object {
        const val CHANNEL_ALARM = "alarm_channel"
        const val CHANNEL_TIMER = "timer_channel"
        const val CHANNEL_UPCOMING = "upcoming_alarm_channel"
    }
}
