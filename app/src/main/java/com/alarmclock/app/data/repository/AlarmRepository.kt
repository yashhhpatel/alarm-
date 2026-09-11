package com.alarmclock.app.data.repository

import com.alarmclock.app.data.local.AlarmDao
import com.alarmclock.app.data.local.AlarmEntity
import kotlinx.coroutines.flow.Flow

class AlarmRepository(private val dao: AlarmDao) {
    fun observeAll(): Flow<List<AlarmEntity>> = dao.observeAll()
    suspend fun getById(id: Long) = dao.getById(id)
    suspend fun getEnabled() = dao.getEnabled()
    suspend fun upsert(alarm: AlarmEntity): Long = dao.upsert(alarm)
    suspend fun delete(alarm: AlarmEntity) = dao.delete(alarm)
    suspend fun setEnabled(alarm: AlarmEntity, enabled: Boolean) = dao.update(alarm.copy(enabled = enabled))
}
