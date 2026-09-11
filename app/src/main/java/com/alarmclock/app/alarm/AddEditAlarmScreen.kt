package com.alarmclock.app.alarm

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alarmclock.app.R
import com.alarmclock.app.common.components.WheelPicker
import com.alarmclock.app.common.util.AlarmTimeUtils
import com.alarmclock.app.common.util.rememberAppViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAlarmScreen(
    alarmId: Long?,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val defaultLabel = stringResource(R.string.default_alarm_label)
    val defaultSoundName = stringResource(R.string.default_sound_name)
    val viewModel = rememberAppViewModel { app ->
        AddEditAlarmViewModel(app.alarmRepository, AlarmScheduler(app), alarmId, defaultLabel, defaultSoundName)
    }
    val form by viewModel.form.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    val ringtonePicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            val name = uri?.let { RingtoneManager.getRingtone(context, it)?.getTitle(context) }
                ?: context.getString(R.string.silent)
            viewModel.update { it.copy(soundUri = uri?.toString(), soundName = name) }
        }
    }

    if (!form.loaded) return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (alarmId == null || alarmId < 0) stringResource(R.string.add_alarm_title)
                        else stringResource(R.string.edit_alarm_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back_cd))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val hour12 = if (form.hour % 12 == 0) 12 else form.hour % 12
                WheelPicker(
                    range = 1..12,
                    selected = hour12,
                    onSelectedChange = { newHour12 ->
                        val isPm = form.hour >= 12
                        val newHour24 = toHour24(newHour12, isPm)
                        viewModel.update { it.copy(hour = newHour24) }
                    }
                )
                Text(":", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                WheelPicker(
                    range = 0..59,
                    selected = form.minute,
                    onSelectedChange = { newMinute -> viewModel.update { it.copy(minute = newMinute) } }
                )
                Column {
                    val isPm = form.hour >= 12
                    listOf(false, true).forEach { isPmOption ->
                        val selected = isPmOption == isPm
                        Text(
                            if (isPmOption) stringResource(R.string.pm) else stringResource(R.string.am),
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable {
                                    val hour12 = if (form.hour % 12 == 0) 12 else form.hour % 12
                                    viewModel.update { it.copy(hour = toHour24(hour12, isPmOption)) }
                                }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val fmt = remember { SimpleDateFormat("EEE dd MMM yyyy", Locale.getDefault()) }
                Text(fmt.format(java.util.Date(form.dateMillis)), style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = stringResource(R.string.pick_date_cd), tint = MaterialTheme.colorScheme.primary)
                }
            }

            DayOfWeekRow(
                selectedMask = form.repeatDays,
                onToggle = { bit -> viewModel.toggleDay(bit) }
            )

            OutlinedTextField(
                value = form.label,
                onValueChange = { viewModel.update { s -> s.copy(label = it) } },
                label = { Text(stringResource(R.string.title_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )

            SettingsRow(
                title = stringResource(R.string.alarm_sound_label),
                subtitle = form.soundName,
                checked = form.soundEnabled,
                onCheckedChange = { viewModel.update { s -> s.copy(soundEnabled = it) } },
                onClick = {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                    }
                    ringtonePicker.launch(intent)
                }
            )

            SettingsRow(
                title = stringResource(R.string.vibration_label),
                subtitle = stringResource(R.string.chime_tone),
                checked = form.vibrate,
                onCheckedChange = { viewModel.update { s -> s.copy(vibrate = it) } },
                onClick = {}
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.cancel)) }
                TextButton(onClick = { viewModel.save(onDone) }) { Text(stringResource(R.string.save), fontWeight = FontWeight.Bold) }
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = form.dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { viewModel.update { s -> s.copy(dateMillis = it) } }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = state, showModeToggle = true)
        }
    }
}

private fun toHour24(hour12: Int, isPm: Boolean): Int {
    val h = hour12 % 12
    return if (isPm) h + 12 else h
}

@Composable
private fun DayOfWeekRow(selectedMask: Int, onToggle: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val context = LocalContext.current
        AlarmTimeUtils.dayLetters(context).forEachIndexed { index, letter ->
            val bit = 1 shl index
            val selected = (selectedMask and bit) != 0
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onToggle(bit) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    letter,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    }
}
