package com.alarmclock.app.alarm

import android.app.KeyguardManager
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.alarmclock.app.AlarmClockApp
import com.alarmclock.app.R
import com.alarmclock.app.settings.LocaleHelper
import com.alarmclock.app.theme.AlarmClockTheme
import com.alarmclock.app.theme.AppThemeMode
import android.content.Context
import android.media.MediaPlayer

class AlarmRingActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var alarmId: Long = -1

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLockedFlags()

        alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        val label = intent.getStringExtra(AlarmReceiver.EXTRA_LABEL).orEmpty()

        startRinging()

        setContent {
            val app = application as AlarmClockApp
            val settings by app.settingsDataStore.settings.collectAsState(initial = null)
            AlarmClockTheme(themeMode = settings?.themeMode ?: AppThemeMode.AUTO) {
                AlarmRingScreen(
                    label = label.ifBlank { getString(R.string.alarm_ring_default_label) },
                    onDismiss = { stopRingingAndFinish() },
                    onSnooze = { snooze() }
                )
            }
        }
    }

    private fun setShowWhenLockedFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(KeyguardManager::class.java)
            km?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
    }

    private fun startRinging() {
        val uri: Uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            isLooping = true
            try {
                setDataSource(this@AlarmRingActivity, uri)
                prepare()
                start()
            } catch (_: Exception) {
            }
        }

        val pattern = longArrayOf(0, 500, 500)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun snooze() {
        stopRingingAndFinish()
    }

    private fun stopRingingAndFinish() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        val manager = getSystemService(NotificationManager::class.java)
        if (alarmId >= 0) manager.cancel(alarmId.toInt())
        finish()
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        vibrator?.cancel()
        super.onDestroy()
    }
}
