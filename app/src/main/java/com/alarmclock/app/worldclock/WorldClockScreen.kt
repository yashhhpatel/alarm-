package com.alarmclock.app.worldclock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WorldClockScreen(onAddCity: () -> Unit, onSettings: () -> Unit) {
    val viewModel = rememberAppViewModel { app -> WorldClockViewModel(app.worldClockRepository) }
    val clocks by viewModel.uiState.collectAsState()
    val nowMillis by viewModel.nowMillis.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = onAddCity) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_city_cd)) }
                    IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings_cd)) }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Icon(
                Icons.Filled.Public,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f),
                modifier = Modifier
                    .size(360.dp)
                    .align(Alignment.Center)
            )

            Column(modifier = Modifier.fillMaxSize()) {
                val localTime = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.getDefault())
                    .format(Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault()))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(localTime, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.local_time),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (clocks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                stringResource(R.string.add_now),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.clickable(onClick = onAddCity)
                            )
                            Text(
                                stringResource(R.string.add_now_desc),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(clocks, key = { it.id }) { clock ->
                            val zone = ZoneId.of(clock.zoneId)
                            val time = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
                                .format(Instant.ofEpochMilli(nowMillis).atZone(zone))
                            val dayLabel = DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.getDefault())
                                .format(Instant.ofEpochMilli(nowMillis).atZone(zone))

                            Card(
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(clock.cityName, style = MaterialTheme.typography.titleMedium)
                                        Text(dayLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(time, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        IconButton(onClick = { viewModel.removeClock(clock) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.remove_cd), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
