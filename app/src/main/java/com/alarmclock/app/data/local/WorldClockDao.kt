package com.alarmclock.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorldClockDao {
    @Query("SELECT * FROM world_clocks ORDER BY sortOrder")
    fun observeAll(): Flow<List<WorldClockEntity>>

    @Insert
    suspend fun insert(clock: WorldClockEntity): Long

    @Delete
    suspend fun delete(clock: WorldClockEntity)

    @Query("SELECT COUNT(*) FROM world_clocks")
    suspend fun count(): Int
}
