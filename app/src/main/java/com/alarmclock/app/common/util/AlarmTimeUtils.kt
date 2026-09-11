package com.alarmclock.app.common.util

import android.content.Context
import com.alarmclock.app.R
import com.alarmclock.app.data.local.AlarmEntity
import java.util.Calendar

/**
 * Day-of-week bitmask helpers. Bit 0 = Monday ... bit 6 = Sunday.
 */
object AlarmTimeUtils {

    fun dayBit(calendarDayOfWeek: Int): Int {
        // Calendar.SUNDAY=1 .. Calendar.SATURDAY=7  ->  bit index Mon=0..Sun=6
        val mondayIndexed = (calendarDayOfWeek + 5) % 7
        return 1 shl mondayIndexed
    }

    fun repeatsOnAnyDay(repeatDays: Int) = repeatDays != 0

    /** Returns the next trigger time in epoch millis for this alarm, strictly after [from]. */
    fun nextTriggerMillis(alarm: AlarmEntity, from: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = from

        if (!repeatsOnAnyDay(alarm.repeatDays)) {
            val target = Calendar.getInstance()
            target.timeInMillis = alarm.dateMillis
            target.set(Calendar.HOUR_OF_DAY, alarm.hour)
            target.set(Calendar.MINUTE, alarm.minute)
            target.set(Calendar.SECOND, 0)
            target.set(Calendar.MILLISECOND, 0)
            if (target.timeInMillis <= from) {
                // one-time alarm already in the past relative to "from" — push to same time tomorrow
                target.add(Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis
        }

        // Repeating: find the earliest of the next 8 days (today..+7) matching a set day bit
        // and with the alarm's hour:minute still ahead of "from" for day offset 0.
        for (offset in 0..7) {
            val candidate = Calendar.getInstance()
            candidate.timeInMillis = from
            candidate.add(Calendar.DAY_OF_YEAR, offset)
            candidate.set(Calendar.HOUR_OF_DAY, alarm.hour)
            candidate.set(Calendar.MINUTE, alarm.minute)
            candidate.set(Calendar.SECOND, 0)
            candidate.set(Calendar.MILLISECOND, 0)

            val bit = dayBit(candidate.get(Calendar.DAY_OF_WEEK))
            val dayMatches = (alarm.repeatDays and bit) != 0
            if (dayMatches && candidate.timeInMillis > from) {
                return candidate.timeInMillis
            }
        }
        // Fallback (shouldn't happen): one week from now at the given time
        cal.add(Calendar.DAY_OF_YEAR, 7)
        cal.set(Calendar.HOUR_OF_DAY, alarm.hour)
        cal.set(Calendar.MINUTE, alarm.minute)
        return cal.timeInMillis
    }

    fun repeatSummary(context: Context, repeatDays: Int, dateMillis: Long): String {
        if (repeatDays == 0) {
            val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
            val fmt = java.text.SimpleDateFormat("EEE, dd MMM", java.util.Locale.getDefault())
            return fmt.format(cal.time)
        }
        if (repeatDays == 0b1111111) return context.getString(R.string.every_day)
        val labels = context.resources.getStringArray(R.array.day_short_names)
        val selected = labels.filterIndexed { index, _ -> (repeatDays and (1 shl index)) != 0 }
        return selected.joinToString(", ")
    }

    fun dayLetters(context: Context): List<String> =
        context.resources.getStringArray(R.array.day_letters).toList()
}
