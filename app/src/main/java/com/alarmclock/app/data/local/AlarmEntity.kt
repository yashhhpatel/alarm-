package com.alarmclock.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val hour: Int,
    val minute: Int,
    /** Bitmask, bit 0 = Monday ... bit 6 = Sunday. 0 = one-time (non-repeating). */
    val repeatDays: Int,
    /** Epoch millis of the date this alarm is scheduled for. Used to compute the next trigger
     * both for one-time alarms and as a reference for repeating ones. */
    val dateMillis: Long,
    val enabled: Boolean,
    val soundUri: String?,
    val soundName: String,
    val soundEnabled: Boolean = true,
    val vibrate: Boolean,
    val vibrationPattern: String = "Chime tone"
)
