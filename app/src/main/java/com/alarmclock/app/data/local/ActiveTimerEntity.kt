package com.alarmclock.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_timers")
data class ActiveTimerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val totalMillis: Long,
    /** Millis remaining at the moment this row was last written (used when paused). */
    val remainingMillis: Long,
    /** Wall-clock time this timer will end, if running. Ignored while paused. */
    val endAtMillis: Long,
    val isPaused: Boolean,
    val createdAt: Long
)
