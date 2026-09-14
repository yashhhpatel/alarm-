package com.alarmclock.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_presets")
data class TimerPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val totalSeconds: Long,
    val sortOrder: Int,
    val isDefault: Boolean = false
)
