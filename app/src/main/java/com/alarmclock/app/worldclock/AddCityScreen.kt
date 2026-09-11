package com.alarmclock.app.worldclock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alarmclock.app.R
import com.alarmclock.app.common.util.rememberAppViewModel

@Composable
fun AddCityScreen(onBack: () -> Unit, onCityAdded: () -> Unit) {
    val viewModel = rememberAppViewModel { app -> WorldClockViewModel(app.worldClockRepository) }
    var query by remember { mutableStateOf("") }
    val results = remember(query) { TimeZoneRepository.search(query) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back_cd)) }
                },
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text(stringResource(R.string.city_country_region)) },
                        singleLine = true,
                        trailingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                items(results, key = { it.zoneId }) { city ->
                    ListItem(
                        headlineContent = { Text("${city.cityName} / ${city.continent}") },
                        supportingContent = {
                            Text(city.gmtOffsetLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        modifier = Modifier.clickable {
                            viewModel.addCity(city)
                            onCityAdded()
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
