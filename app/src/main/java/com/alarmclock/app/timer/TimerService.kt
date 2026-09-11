package com.alarmclock.app.timer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.alarmclock.app.AlarmClockApp
import com.alarmclock.app.MainActivity
import com.alarmclock.app.R
import com.alarmclock.app.data.local.ActiveTimerEntity
import com.alarmclock.app.settings.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerService : Service() {

    private lateinit var app: AlarmClockApp
    private val scope = CoroutineScope(SupervisorJob())
    private var tickerJob: Job? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrapContext(newBase))
    }

    override fun onCreate() {
        super.onCreate()
        app = application as AlarmClockApp
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Must be called synchronously within onStartCommand to satisfy the foreground-service
        // contract; the mutation below may complete later on a background dispatch.
        ensureForeground()
        when (intent?.action) {
            ACTION_START -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: getString(R.string.default_timer_title)
                val totalMillis = intent.getLongExtra(EXTRA_TOTAL_MILLIS, 0L)
                scope.launch {
                    app.timerRepository.addActive(
                        ActiveTimerEntity(
                            title = title,
                            totalMillis = totalMillis,
                            remainingMillis = totalMillis,
                            endAtMillis = System.currentTimeMillis() + totalMillis,
                            isPaused = false,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    // Only start the ticker once the new timer is actually persisted, otherwise
                    // the ticker's first tick() can race the insert, see an empty active list,
                    // and immediately stop the service before the timer ever gets a chance to run.
                    startTicker()
                }
            }
            ACTION_PAUSE -> {
                val id = intent.getLongExtra(EXTRA_ID, -1L)
                scope.launch { pauseTimer(id); startTicker() }
            }
            ACTION_RESUME -> {
                val id = intent.getLongExtra(EXTRA_ID, -1L)
                scope.launch { resumeTimer(id); startTicker() }
            }
            ACTION_CANCEL -> {
                val id = intent.getLongExtra(EXTRA_ID, -1L)
                scope.launch { cancelTimer(id) }
            }
            else -> startTicker()
        }
        return START_STICKY
    }

    private suspend fun pauseTimer(id: Long) {
        val timers = app.timerRepository.getActive()
        val t = timers.find { it.id == id } ?: return
        if (t.isPaused) return
        val remaining = (t.endAtMillis - System.currentTimeMillis()).coerceAtLeast(0)
        app.timerRepository.updateActive(t.copy(remainingMillis = remaining, isPaused = true))
    }

    private suspend fun resumeTimer(id: Long) {
        val timers = app.timerRepository.getActive()
        val t = timers.find { it.id == id } ?: return
        if (!t.isPaused) return
        app.timerRepository.updateActive(
            t.copy(endAtMillis = System.currentTimeMillis() + t.remainingMillis, isPaused = false)
        )
    }

    private suspend fun cancelTimer(id: Long) {
        val timers = app.timerRepository.getActive()
        val t = timers.find { it.id == id } ?: return
        app.timerRepository.removeActive(t)
        maybeStop()
    }

    private fun startTicker() {
        if (tickerJob != null) return
        tickerJob = scope.launch {
            while (true) {
                tick()
                delay(1000)
            }
        }
    }

    private suspend fun tick() {
        val timers = app.timerRepository.getActive()
        if (timers.isEmpty()) {
            maybeStop()
            return
        }
        val now = System.currentTimeMillis()
        timers.forEach { t ->
            if (!t.isPaused && t.endAtMillis <= now) {
                onTimerComplete(t)
            }
        }
        updateNotification()
    }

    private suspend fun onTimerComplete(timer: ActiveTimerEntity) {
        app.timerRepository.removeActive(timer)
        playCompletionAlert(timer.title)
    }

    private fun playCompletionAlert(title: String) {
        try {
            val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone = RingtoneManager.getRingtone(this, uri)
            ringtone.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            ringtone.play()
        } catch (_: Exception) {
        }
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(800)
        }

        val manager = getSystemService(android.app.NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, AlarmClockApp.CHANNEL_ALARM)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(getString(R.string.notif_timer_finished_title, title))
            .setContentText(getString(R.string.notif_timer_completed_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }

    private suspend fun updateNotification() {
        val timers = app.timerRepository.getActive()
        if (timers.isEmpty()) return
        val now = System.currentTimeMillis()
        val nearest = timers.minByOrNull { if (it.isPaused) it.remainingMillis else (it.endAtMillis - now) }
        val remaining = nearest?.let { if (it.isPaused) it.remainingMillis else (it.endAtMillis - now).coerceAtLeast(0) } ?: 0L
        val countText = formatMillis(remaining)
        val subText = if (timers.size == 1) {
            getString(R.string.notif_timers_running_one)
        } else {
            getString(R.string.notif_timers_running_many, timers.size)
        }

        val manager = getSystemService(android.app.NotificationManager::class.java)
        manager.notify(NOTIF_ID, buildNotification(countText, subText))
    }

    private fun buildNotification(countText: String, subText: String): Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, AlarmClockApp.CHANNEL_TIMER)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(countText)
            .setContentText(subText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun ensureForeground() {
        startForeground(NOTIF_ID, buildNotification("00:00:00", getString(R.string.notif_starting)))
    }

    private suspend fun maybeStop() {
        val timers = app.timerRepository.getActive()
        if (timers.isEmpty()) {
            tickerJob?.cancel()
            tickerJob = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.alarmclock.app.timer.START"
        const val ACTION_PAUSE = "com.alarmclock.app.timer.PAUSE"
        const val ACTION_RESUME = "com.alarmclock.app.timer.RESUME"
        const val ACTION_CANCEL = "com.alarmclock.app.timer.CANCEL"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TOTAL_MILLIS = "extra_total_millis"
        const val EXTRA_ID = "extra_id"
        const val NOTIF_ID = 5001

        fun formatMillis(millis: Long): String {
            val totalSeconds = millis / 1000
            val h = totalSeconds / 3600
            val m = (totalSeconds % 3600) / 60
            val s = totalSeconds % 60
            return String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
        }
    }
}
