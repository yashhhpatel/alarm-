package com.alarmclock.app.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmclock.app.data.local.AlarmEntity
import com.alarmclock.app.data.repository.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class AlarmFormState(
    val id: Long = 0,
    val hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    val minute: Int = Calendar.getInstance().get(Calendar.MINUTE),
    val dateMillis: Long = System.currentTimeMillis(),
    val repeatDays: Int = 0,
    val label: String = "",
    val soundName: String = "",
    val soundUri: String? = null,
    val soundEnabled: Boolean = true,
    val vibrate: Boolean = true,
    val loaded: Boolean = false
)

class AddEditAlarmViewModel(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler,
    private val alarmId: Long?,
    defaultLabel: String,
    defaultSoundName: String
) : ViewModel() {

    private val _form = MutableStateFlow(
        AlarmFormState(label = defaultLabel, soundName = defaultSoundName, loaded = alarmId == null)
    )
    val form: StateFlow<AlarmFormState> = _form.asStateFlow()

    init {
        if (alarmId != null && alarmId >= 0) {
            viewModelScope.launch {
                repository.getById(alarmId)?.let { alarm ->
                    _form.value = AlarmFormState(
                        id = alarm.id,
                        hour = alarm.hour,
                        minute = alarm.minute,
                        dateMillis = alarm.dateMillis,
                        repeatDays = alarm.repeatDays,
                        label = alarm.label,
                        soundName = alarm.soundName,
                        soundUri = alarm.soundUri,
                        soundEnabled = alarm.soundEnabled,
                        vibrate = alarm.vibrate,
                        loaded = true
                    )
                } ?: run { _form.value = _form.value.copy(loaded = true) }
            }
        }
    }

    fun update(transform: (AlarmFormState) -> AlarmFormState) {
        _form.value = transform(_form.value)
    }

    fun toggleDay(bit: Int) {
        _form.value = _form.value.let { it.copy(repeatDays = it.repeatDays xor bit) }
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            val f = _form.value
            val entity = AlarmEntity(
                id = f.id,
                label = f.label,
                hour = f.hour,
                minute = f.minute,
                repeatDays = f.repeatDays,
                dateMillis = f.dateMillis,
                enabled = true,
                soundUri = f.soundUri,
                soundName = f.soundName,
                soundEnabled = f.soundEnabled,
                vibrate = f.vibrate
            )
            val newId = repository.upsert(entity)
            scheduler.schedule(entity.copy(id = if (f.id != 0L) f.id else newId))
            onDone()
        }
    }
}
