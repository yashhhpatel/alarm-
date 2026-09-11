package com.alarmclock.app.stopwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StopwatchUiState(
    val elapsedMillis: Long = 0L,
    val running: Boolean = false,
    val laps: List<Long> = emptyList()
)

class StopwatchViewModel : ViewModel() {

    private val _state = MutableStateFlow(StopwatchUiState())
    val state: StateFlow<StopwatchUiState> = _state.asStateFlow()

    private var tickJob: Job? = null
    private var startedAtRealtime = 0L
    private var accumulatedBeforeStart = 0L

    fun start() {
        if (_state.value.running) return
        startedAtRealtime = System.currentTimeMillis()
        accumulatedBeforeStart = _state.value.elapsedMillis
        _state.value = _state.value.copy(running = true)
        tickJob = viewModelScope.launch {
            while (true) {
                val elapsed = accumulatedBeforeStart + (System.currentTimeMillis() - startedAtRealtime)
                _state.value = _state.value.copy(elapsedMillis = elapsed)
                delay(29)
            }
        }
    }

    fun pause() {
        tickJob?.cancel()
        tickJob = null
        _state.value = _state.value.copy(running = false)
    }

    fun lap() {
        if (!_state.value.running) return
        _state.value = _state.value.copy(laps = _state.value.laps + _state.value.elapsedMillis)
    }

    fun reset() {
        tickJob?.cancel()
        tickJob = null
        _state.value = StopwatchUiState()
    }
}
