package com.alarmclock.app.data.repository

import com.alarmclock.app.data.local.WorldClockDao
import com.alarmclock.app.data.local.WorldClockEntity
import kotlinx.coroutines.flow.Flow

class WorldClockRepository(private val dao: WorldClockDao) {
    fun observeAll(): Flow<List<WorldClockEntity>> = dao.observeAll()
    suspend fun add(clock: WorldClockEntity) {
        val order = dao.count()
        dao.insert(clock.copy(sortOrder = order))
    }
    suspend fun delete(clock: WorldClockEntity) = dao.delete(clock)
}
