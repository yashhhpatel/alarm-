package com.alarmclock.app.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.alarmclock.app.AlarmClockApp
import com.alarmclock.app.R
import com.alarmclock.app.common.util.AlarmTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmScheduler.ACTION_FIRE_ALARM) return
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        if (alarmId < 0) return

        val pendingResult = goAsync()
        val app = context.applicationContext as AlarmClockApp

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarm = app.alarmRepository.getById(alarmId) ?: return@launch
                if (!alarm.enabled) return@launch

                launchRingUi(context, alarmId, alarm.label)

                if (AlarmTimeUtils.repeatsOnAnyDay(alarm.repeatDays)) {
                    AlarmScheduler(context).schedule(alarm)
                } else {
                    app.alarmRepository.setEnabled(alarm, false)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun launchRingUi(context: Context, alarmId: Long, label: String) {
        val fullScreenIntent = Intent(context, AlarmRingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_LABEL, label)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, AlarmClockApp.CHANNEL_ALARM)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(label.ifBlank { context.getString(R.string.alarm_ring_default_label) })
            .setContentText(context.getString(R.string.notif_tap_to_open))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(alarmId.toInt(), notification)

        context.startActivity(fullScreenIntent)
    }

    companion object {
        const val EXTRA_LABEL = "extra_label"
    }
}
