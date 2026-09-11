package com.alarmclock.app.worldclock

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale

data class CityTimeZone(
    val zoneId: String,
    val cityName: String,
    val continent: String,
    val gmtOffsetLabel: String
)

object TimeZoneRepository {

    val all: List<CityTimeZone> by lazy {
        ZoneId.getAvailableZoneIds()
            .filter { it.contains('/') && !it.startsWith("Etc/") && !it.startsWith("SystemV") }
            .mapNotNull { id ->
                val parts = id.split('/')
                if (parts.size < 2) return@mapNotNull null
                val continent = parts[0].replace('_', ' ')
                val city = parts.last().replace('_', ' ')
                val zone = ZoneId.of(id)
                val offset = ZonedDateTime.now(zone).offset
                val totalMinutes = offset.totalSeconds / 60
                val sign = if (totalMinutes >= 0) "+" else "-"
                val hh = kotlin.math.abs(totalMinutes) / 60
                val mm = kotlin.math.abs(totalMinutes) % 60
                val label = if (mm == 0) "GMT $sign$hh" else "GMT $sign$hh:${mm.toString().padStart(2, '0')}"
                CityTimeZone(id, city, continent, label)
            }
            .distinctBy { it.zoneId }
            .sortedBy { it.cityName }
    }

    fun search(query: String): List<CityTimeZone> {
        if (query.isBlank()) return emptyList()
        val q = query.trim()
        return all.filter {
            it.cityName.contains(q, ignoreCase = true) || it.continent.contains(q, ignoreCase = true)
        }.take(60)
    }
}
