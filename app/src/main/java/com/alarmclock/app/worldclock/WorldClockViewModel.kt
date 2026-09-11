package com.alarmclock.app.worldclock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmclock.app.data.local.WorldClockEntity
import com.alarmclock.app.data.repository.WorldClockRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorldClockViewModel(private val repository: WorldClockRepository) : ViewModel() {

    private val ticker = kotlinx.coroutines.flow.flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000)
        }
    }

    val uiState: StateFlow<List<WorldClockEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nowMillis: StateFlow<Long> = ticker.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), System.currentTimeMillis())

    fun addCity(city: CityTimeZone) {
        viewModelScope.launch {
            repository.add(
                WorldClockEntity(
                    zoneId = city.zoneId,
                    cityName = city.cityName,
                    continent = city.continent,
                    sortOrder = 0
                )
            )
        }
    }

    fun removeClock(clock: WorldClockEntity) {
        viewModelScope.launch { repository.delete(clock) }
    }
}
