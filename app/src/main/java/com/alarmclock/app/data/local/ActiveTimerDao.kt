package com.alarmclock.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveTimerDao {
    @Query("SELECT * FROM active_timers ORDER BY createdAt")
    fun observeAll(): Flow<List<ActiveTimerEntity>>

    @Query("SELECT * FROM active_timers ORDER BY createdAt")
    suspend fun getAll(): List<ActiveTimerEntity>

    @Insert
    suspend fun insert(timer: ActiveTimerEntity): Long

    @Update
    suspend fun update(timer: ActiveTimerEntity)

    @Delete
    suspend fun delete(timer: ActiveTimerEntity)

    @Query("DELETE FROM active_timers WHERE id = :id")
    suspend fun deleteById(id: Long)
}
