package com.alarmclock.app.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmclock.app.data.local.AlarmEntity
import com.alarmclock.app.data.repository.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NextAlarmCountdown(val hours: Int, val minutes: Int)

data class AlarmListUiState(
    val alarms: List<AlarmEntity> = emptyList(),
    val nextAlarmCountdown: NextAlarmCountdown? = null
)

class AlarmViewModel(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler
) : ViewModel() {

    val uiState: StateFlow<AlarmListUiState> = repository.observeAll()
        .map { alarms ->
            AlarmListUiState(
                alarms = alarms,
                nextAlarmCountdown = computeCountdown(alarms)
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlarmListUiState())

    private var searchQuery = MutableStateFlow("")
    val searchResults: StateFlow<List<AlarmEntity>> = combine(repository.observeAll(), searchQuery) { alarms, query ->
        if (query.isBlank()) emptyList() else alarms.filter {
            it.label.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun currentQuery() = searchQuery.value

    fun toggleAlarm(alarm: AlarmEntity, enabled: Boolean) {
        viewModelScope.launch {
            repository.setEnabled(alarm, enabled)
            val updated = alarm.copy(enabled = enabled)
            if (enabled) scheduler.schedule(updated) else scheduler.cancel(updated)
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            scheduler.cancel(alarm)
            repository.delete(alarm)
        }
    }

    private fun computeCountdown(alarms: List<AlarmEntity>): NextAlarmCountdown? {
        val enabled = alarms.filter { it.enabled }
        if (enabled.isEmpty()) return null
        val now = System.currentTimeMillis()
        val next = enabled.minOfOrNull { com.alarmclock.app.common.util.AlarmTimeUtils.nextTriggerMillis(it, now) }
            ?: return null
        val diff = next - now
        if (diff <= 0) return null
        val totalMinutes = diff / 60000
        val hours = (totalMinutes / 60).toInt()
        val minutes = (totalMinutes % 60).toInt()
        return NextAlarmCountdown(hours, minutes)
    }
}
