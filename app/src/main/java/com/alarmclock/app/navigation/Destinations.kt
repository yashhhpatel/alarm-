package com.alarmclock.app.navigation

object Destinations {
    const val SPLASH = "splash"
    const val PERMISSIONS = "permissions"
    const val CONSENT = "consent"

    const val ALARM_LIST = "alarm_list"
    const val ADD_EDIT_ALARM = "add_edit_alarm"
    const val ADD_EDIT_ALARM_ARG = "alarmId"
    const val ADD_EDIT_ALARM_ROUTE = "$ADD_EDIT_ALARM?$ADD_EDIT_ALARM_ARG={$ADD_EDIT_ALARM_ARG}"
    fun addEditAlarmRoute(alarmId: Long? = null) = "$ADD_EDIT_ALARM?$ADD_EDIT_ALARM_ARG=${alarmId ?: -1L}"
    const val ALARM_SEARCH = "alarm_search"

    const val WORLD_CLOCK = "world_clock"
    const val ADD_CITY = "add_city"

    const val STOPWATCH = "stopwatch"

    const val TIMER = "timer"

    const val SETTINGS = "settings"
    const val CHANGE_LANGUAGE = "change_language"
    const val PRIVACY_SETTINGS = "privacy_settings"

    val bottomNavRoutes = listOf(ALARM_LIST, WORLD_CLOCK, STOPWATCH, TIMER)
}
