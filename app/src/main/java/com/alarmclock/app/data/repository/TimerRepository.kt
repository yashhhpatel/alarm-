package com.alarmclock.app.data.repository

import com.alarmclock.app.data.local.ActiveTimerDao
import com.alarmclock.app.data.local.ActiveTimerEntity
import com.alarmclock.app.data.local.TimerPresetDao
import com.alarmclock.app.data.local.TimerPresetEntity
import kotlinx.coroutines.flow.Flow

class TimerRepository(
    private val presetDao: TimerPresetDao,
    private val activeDao: ActiveTimerDao
) {
    fun observePresets(): Flow<List<TimerPresetEntity>> = presetDao.observeAll()
    suspend fun addPreset(preset: TimerPresetEntity): Long = presetDao.insert(preset)
    suspend fun updatePreset(preset: TimerPresetEntity) = presetDao.update(preset)
    suspend fun deletePreset(preset: TimerPresetEntity) = presetDao.delete(preset)

    fun observeActive(): Flow<List<ActiveTimerEntity>> = activeDao.observeAll()
    suspend fun getActive(): List<ActiveTimerEntity> = activeDao.getAll()
    suspend fun addActive(timer: ActiveTimerEntity): Long = activeDao.insert(timer)
    suspend fun updateActive(timer: ActiveTimerEntity) = activeDao.update(timer)
    suspend fun removeActive(timer: ActiveTimerEntity) = activeDao.delete(timer)
}
