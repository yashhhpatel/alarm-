package com.alarmclock.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "world_clocks")
data class WorldClockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val zoneId: String,
    val cityName: String,
    val continent: String,
    val sortOrder: Int
)
