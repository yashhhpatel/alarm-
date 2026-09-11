package com.alarmclock.app.timer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alarmclock.app.R
import com.alarmclock.app.common.components.WheelPicker
import com.alarmclock.app.common.util.currentApp
import com.alarmclock.app.common.util.rememberAppViewModel
import com.alarmclock.app.data.local.TimerPresetEntity

private val WHEEL_ITEM_WIDTH = 72.dp
private val WHEEL_SEPARATOR_WIDTH = 24.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(onSettings: () -> Unit) {
    val app = currentApp()
    val viewModel = rememberAppViewModel { TimerViewModel(app.timerRepository, app) }
    val presets by viewModel.presets.collectAsState()
    val active by viewModel.activeTimers.collectAsState()

    var hours by remember { mutableStateOf(0) }
    var minutes by remember { mutableStateOf(1) }
    var seconds by remember { mutableStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }
    var editingPreset by remember { mutableStateOf<TimerPresetEntity?>(null) }
    var selectedPresetId by remember { mutableStateOf<Long?>(null) }
    val defaultTimerTitle = stringResource(R.string.default_timer_title)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings_cd))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                active.forEach { timer ->
                    RunningTimerCard(
                        timer = timer,
                        onPause = { viewModel.pause(timer.id) },
                        onResume = { viewModel.resume(timer.id) },
                        onCancel = { viewModel.cancel(timer.id) }
                    )
                }

                Spacer(Modifier.height(24.dp))
                TimeWheelSection(
                    hours = hours,
                    minutes = minutes,
                    seconds = seconds,
                    onHoursChange = { hours = it },
                    onMinutesChange = { minutes = it },
                    onSecondsChange = { seconds = it }
                )
                Spacer(Modifier.height(24.dp))

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        presets.forEach { preset ->
                            PresetChip(
                                preset = preset,
                                selected = preset.id == selectedPresetId,
                                onClick = {
                                    selectedPresetId = preset.id
                                    val total = preset.totalSeconds
                                    hours = (total / 3600).toInt()
                                    minutes = ((total % 3600) / 60).toInt()
                                    seconds = (total % 60).toInt()
                                },
                                onLongClick = { editingPreset = preset }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { selectedPresetId?.let { id -> editingPreset = presets.find { it.id == id } } },
                        enabled = selectedPresetId != null,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.edit_button), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = { showAddSheet = true },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.add_button), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        val totalMillis = (hours * 3600L + minutes * 60L + seconds) * 1000L
                        if (totalMillis > 0) viewModel.startTimer(defaultTimerTitle, totalMillis)
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(56.dp)
                ) {
                    Text(stringResource(R.string.start_button), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showAddSheet) {
        val sheetState = rememberModalBottomSheetState()
        var h by remember { mutableStateOf(0) }
        var m by remember { mutableStateOf(0) }
        var s by remember { mutableStateOf(0) }
        ModalBottomSheet(onDismissRequest = { showAddSheet = false }, sheetState = sheetState) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.add_preset_timer_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                TimeWheelSection(
                    hours = h,
                    minutes = m,
                    seconds = s,
                    onHoursChange = { h = it },
                    onMinutesChange = { m = it },
                    onSecondsChange = { s = it }
                )
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        presets.forEach { preset ->
                            PresetChip(
                                preset = preset,
                                selected = false,
                                onClick = {
                                    val total = preset.totalSeconds
                                    h = (total / 3600).toInt()
                                    m = ((total % 3600) / 60).toInt()
                                    s = (total % 60).toInt()
                                },
                                onLongClick = {}
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showAddSheet = false }) { Text(stringResource(R.string.cancel)) }
                    TextButton(onClick = {
                        val total = h * 3600L + m * 60L + s
                        if (total > 0) viewModel.addPreset("", total)
                        showAddSheet = false
                    }) { Text(stringResource(R.string.add_button), fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    editingPreset?.let { preset ->
        var title by remember(preset.id) { mutableStateOf(preset.title) }
        AlertDialog(
            onDismissRequest = { editingPreset = null },
            title = { Text(stringResource(R.string.edit_preset_timer_title), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(TimerService.formatMillis(preset.totalSeconds * 1000), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = title, onValueChange = { title = it }, placeholder = { Text(stringResource(R.string.add_title_placeholder)) })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updatePreset(preset.copy(title = title))
                    editingPreset = null
                }) { Text(stringResource(R.string.add_button)) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.deletePreset(preset)
                        if (selectedPresetId == preset.id) selectedPresetId = null
                        editingPreset = null
                    }) { Text(stringResource(R.string.delete)) }
                    TextButton(onClick = { editingPreset = null }) { Text(stringResource(R.string.cancel)) }
                }
            }
        )
    }
}

@Composable
private fun TimeWheelSection(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            Text(
                stringResource(R.string.hours_label),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(WHEEL_ITEM_WIDTH)
            )
            Spacer(Modifier.width(WHEEL_SEPARATOR_WIDTH))
            Text(
                stringResource(R.string.minutes_label),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(WHEEL_ITEM_WIDTH)
            )
            Spacer(Modifier.width(WHEEL_SEPARATOR_WIDTH))
            Text(
                stringResource(R.string.seconds_label),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(WHEEL_ITEM_WIDTH)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            WheelPicker(range = 0..23, selected = hours, onSelectedChange = onHoursChange)
            Box(modifier = Modifier.width(WHEEL_SEPARATOR_WIDTH), contentAlignment = Alignment.Center) {
                Text(":", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
            WheelPicker(range = 0..59, selected = minutes, onSelectedChange = onMinutesChange)
            Box(modifier = Modifier.width(WHEEL_SEPARATOR_WIDTH), contentAlignment = Alignment.Center) {
                Text(":", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
            WheelPicker(range = 0..59, selected = seconds, onSelectedChange = onSecondsChange)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PresetChip(preset: TimerPresetEntity, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(
                if (selected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier
                }
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        val total = preset.totalSeconds
        val h = total / 3600
        val m = (total % 3600) / 60
        val label = if (h > 0) String.format("%02d:%02d:%02d", h, m, total % 60) else String.format("%02d:%02d", m, total % 60)
        Text(preset.title.ifBlank { label }, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
    }
}

@Composable
private fun RunningTimerCard(
    timer: ActiveTimerUi,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(timer.title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        TimerService.formatMillis(timer.remainingMillis),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.Cancel, contentDescription = stringResource(R.string.cancel_cd), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { if (timer.isPaused) onResume() else onPause() }) {
                        Icon(
                            if (timer.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = if (timer.isPaused) stringResource(R.string.resume_cd) else stringResource(R.string.pause_cd),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            val progress = if (timer.totalMillis > 0) {
                1f - (timer.remainingMillis.toFloat() / timer.totalMillis.toFloat())
            } else 0f
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
