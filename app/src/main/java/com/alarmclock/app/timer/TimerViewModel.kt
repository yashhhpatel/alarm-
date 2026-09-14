package com.alarmclock.app.timer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmclock.app.data.local.ActiveTimerEntity
import com.alarmclock.app.data.local.TimerPresetEntity
import com.alarmclock.app.data.repository.TimerRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActiveTimerUi(
    val id: Long,
    val title: String,
    val totalMillis: Long,
    val remainingMillis: Long,
    val isPaused: Boolean,
    val startedAt: Long
)

class TimerViewModel(
    private val repository: TimerRepository,
    private val appContext: Context
) : ViewModel() {

    private val ticker = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(500)
        }
    }

    val presets: StateFlow<List<TimerPresetEntity>> = repository.observePresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTimers: StateFlow<List<ActiveTimerUi>> = combine(repository.observeActive(), ticker) { timers, now ->
        timers.map { t ->
            val remaining = if (t.isPaused) t.remainingMillis else (t.endAtMillis - now).coerceAtLeast(0)
            ActiveTimerUi(
                id = t.id,
                title = t.title,
                totalMillis = t.totalMillis,
                remainingMillis = remaining,
                isPaused = t.isPaused,
                startedAt = t.createdAt
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            if (repository.observePresets().first().isEmpty()) {
                listOf(60L, 180L, 300L).forEachIndexed { index, seconds ->
                    repository.addPreset(TimerPresetEntity(title = "", totalSeconds = seconds, sortOrder = index, isDefault = true))
                }
            }
        }
    }

    fun startTimer(title: String, totalMillis: Long) {
        val resolvedTitle = title.ifBlank { appContext.getString(com.alarmclock.app.R.string.default_timer_title) }
        TimerController.start(appContext, resolvedTitle, totalMillis)
    }

    fun pause(id: Long) = TimerController.pause(appContext, id)
    fun resume(id: Long) = TimerController.resume(appContext, id)
    fun cancel(id: Long) = TimerController.cancel(appContext, id)

    fun addPreset(title: String, totalSeconds: Long) {
        if (presets.value.any { it.totalSeconds == totalSeconds }) return
        viewModelScope.launch {
            repository.addPreset(TimerPresetEntity(title = title, totalSeconds = totalSeconds, sortOrder = presets.value.size))
        }
    }

    fun updatePreset(preset: TimerPresetEntity) {
        viewModelScope.launch { repository.updatePreset(preset) }
    }

    fun deletePreset(preset: TimerPresetEntity) {
        viewModelScope.launch { repository.deletePreset(preset) }
    }
}
