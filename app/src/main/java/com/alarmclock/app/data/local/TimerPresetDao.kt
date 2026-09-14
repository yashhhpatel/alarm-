package com.alarmclock.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerPresetDao {
    @Query("SELECT * FROM timer_presets ORDER BY sortOrder")
    fun observeAll(): Flow<List<TimerPresetEntity>>

    @Insert
    suspend fun insert(preset: TimerPresetEntity): Long

    @Update
    suspend fun update(preset: TimerPresetEntity)

    @Delete
    suspend fun delete(preset: TimerPresetEntity)

    @Query("DELETE FROM timer_presets WHERE isDefault = 0 AND totalSeconds = :totalSeconds")
    suspend fun deleteNonDefaultByDuration(totalSeconds: Long)
}
