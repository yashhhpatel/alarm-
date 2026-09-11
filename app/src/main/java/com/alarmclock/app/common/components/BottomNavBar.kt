package com.alarmclock.app.common.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.alarmclock.app.R
import com.alarmclock.app.navigation.Destinations

private data class NavItem(
    val route: String,
    val label: String,
    val outlined: ImageVector,
    val filled: ImageVector
)

@Composable
fun AppBottomNavBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    val items = listOf(
        NavItem(Destinations.ALARM_LIST, stringResource(R.string.nav_alarm), Icons.Outlined.Alarm, Icons.Filled.Alarm),
        NavItem(Destinations.WORLD_CLOCK, stringResource(R.string.nav_world), Icons.Outlined.Public, Icons.Filled.Public),
        NavItem(Destinations.STOPWATCH, stringResource(R.string.nav_stop), Icons.Outlined.Timer, Icons.Filled.Timer),
        NavItem(Destinations.TIMER, stringResource(R.string.nav_timer), Icons.Outlined.HourglassBottom, Icons.Filled.HourglassBottom)
    )
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.filled else item.outlined,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}
