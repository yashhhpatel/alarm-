package com.alarmclock.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.alarmclock.app.common.util.AlarmTimeUtils
import com.alarmclock.app.data.local.AlarmEntity

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
        val pendingIntent = buildPendingIntent(alarm)

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
        alarmManager.cancel(buildPendingIntent(alarm))
    }

    fun rescheduleAll(alarms: List<AlarmEntity>) {
        alarms.filter { it.enabled }.forEach { schedule(it) }
    }

    private fun buildPendingIntent(alarm: AlarmEntity): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_FIRE_ALARM
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }
        return PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_FIRE_ALARM = "com.alarmclock.app.ACTION_FIRE_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
    }
}
