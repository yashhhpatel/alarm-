package com.alarmclock.app.alarm

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.alarmclock.app.AlarmClockApp
import com.alarmclock.app.MainActivity
import com.alarmclock.app.R
import com.alarmclock.app.common.util.AlarmTimeUtils
import com.alarmclock.app.data.local.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExact(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true
    }

    fun schedule(alarm: AlarmEntity) {
        if (!alarm.enabled) {
            cancel(alarm)
            return
        }
        val triggerAt = AlarmTimeUtils.nextTriggerMillis(alarm)
        scheduleAt(alarm.id, triggerAt)
        updateUpcomingNotification()
    }

    /** Schedules a one-shot re-ring of [alarmId] roughly [minutesFromNow] minutes from now. */
    fun scheduleSnooze(alarmId: Long, minutesFromNow: Int = SNOOZE_MINUTES) {
        val triggerAt = System.currentTimeMillis() + minutesFromNow * 60_000L
        scheduleAt(alarmId, triggerAt)
    }

    private fun scheduleAt(alarmId: Long, triggerAt: Long) {
        val pendingIntent = buildPendingIntent(alarmId)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerAt, pendingIntent),
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancel(alarm: AlarmEntity) {
        alarmManager.cancel(buildPendingIntent(alarm.id))
        updateUpcomingNotification()
    }

    fun rescheduleAll(alarms: List<AlarmEntity>) {
        alarms.filter { it.enabled }.forEach { schedule(it) }
    }

    /**
     * Recomputes the single soonest enabled alarm across the whole list and shows (or clears) a
     * quiet "upcoming alarm" reminder notification, per the user's Settings toggle.
     */
    fun refreshUpcomingNotification() = updateUpcomingNotification()

    private fun updateUpcomingNotification() {
        val app = context.applicationContext as AlarmClockApp
        val manager = context.getSystemService(NotificationManager::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val enabled = app.settingsDataStore.settings.first().upcomingAlarmNotification
            if (!enabled) {
                manager.cancel(UPCOMING_NOTIF_ID)
                return@launch
            }
            val next = app.alarmRepository.getEnabled()
                .minByOrNull { AlarmTimeUtils.nextTriggerMillis(it) }
            if (next == null) {
                manager.cancel(UPCOMING_NOTIF_ID)
                return@launch
            }
            val now = System.currentTimeMillis()
            val diff = (AlarmTimeUtils.nextTriggerMillis(next) - now).coerceAtLeast(0)
            val totalMinutes = diff / 60_000
            val hours = (totalMinutes / 60).toInt()
            val minutes = (totalMinutes % 60).toInt()
            val text = if (hours > 0) {
                context.getString(R.string.next_alarm_countdown_hm, hours, minutes)
            } else {
                context.getString(R.string.next_alarm_countdown_m, minutes)
            }
            val contentIntent = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, AlarmClockApp.CHANNEL_UPCOMING)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(next.label.ifBlank { context.getString(R.string.alarm_ring_default_label) })
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
            manager.notify(UPCOMING_NOTIF_ID, notification)
        }
    }

    private fun buildPendingIntent(alarmId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_FIRE_ALARM
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_FIRE_ALARM = "com.alarmclock.app.ACTION_FIRE_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val SNOOZE_MINUTES = 5
        const val UPCOMING_NOTIF_ID = 9001
    }
}
