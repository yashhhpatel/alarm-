package com.alarmclock.app.stopwatch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alarmclock.app.R
import com.alarmclock.app.common.util.rememberAppViewModel
import com.alarmclock.app.theme.SuccessGreen
import java.util.Locale

private fun formatElapsed(millis: Long): String {
    val totalCentis = millis / 10
    val minutes = totalCentis / 6000
    val seconds = (totalCentis / 100) % 60
    val centis = totalCentis % 100
    return String.format(Locale.getDefault(), "%02d:%02d.%02d", minutes, seconds, centis)
}

@Composable
fun StopwatchScreen(onSettings: () -> Unit) {
    val viewModel = rememberAppViewModel { StopwatchViewModel() }
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                actions = {
                    IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings_cd)) }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 64.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    formatElapsed(state.elapsedMillis),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp)
            ) {
                if (state.laps.isNotEmpty()) {
                    val segments = state.laps.mapIndexed { index, cumulative ->
                        cumulative - (if (index > 0) state.laps[index - 1] else 0L)
                    }
                    val fastest = segments.min()
                    val slowest = segments.max()

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(R.string.lap_column_header),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            stringResource(R.string.lap_times_header),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            stringResource(R.string.overall_time_header),
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    segments.forEachIndexed { index, segment ->
                        val rowColor = when {
                            segment == fastest -> SuccessGreen
                            segment == slowest && fastest != slowest -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text(
                                (index + 1).toString(),
                                modifier = Modifier.weight(1f),
                                color = rowColor
                            )
                            Text(
                                formatElapsed(segment),
                                modifier = Modifier.weight(1f),
                                color = rowColor
                            )
                            Text(
                                formatElapsed(state.laps[index]),
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                color = rowColor
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { if (state.running) viewModel.lap() else viewModel.reset() },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        if (state.running) stringResource(R.string.lap_cd) else stringResource(R.string.restart_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = { if (state.running) viewModel.pause() else viewModel.start() },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                    colors = if (state.running) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Text(
                        if (state.running) stringResource(R.string.stop_button) else stringResource(R.string.start_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
