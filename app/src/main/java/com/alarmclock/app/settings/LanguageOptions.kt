package com.alarmclock.app.settings

data class LanguageOption(val code: String, val displayName: String)

/**
 * Display names are shown in each language's own native endonym (standard convention for
 * language pickers) regardless of the app's current UI language.
 */
object LanguageOptions {
    val all = listOf(
        LanguageOption("en", "English"),
        LanguageOption("zh", "中文"),
        LanguageOption("hi", "हिन्दी"),
        LanguageOption("fr", "Français"),
        LanguageOption("ar", "العربية"),
        LanguageOption("it", "Italiano"),
        LanguageOption("es", "Español"),
        LanguageOption("bn", "বাংলা"),
        LanguageOption("ru", "Русский"),
        LanguageOption("pt", "Português"),
        LanguageOption("de", "Deutsch"),
        LanguageOption("th", "ไทย"),
        LanguageOption("ja", "日本語"),
        LanguageOption("ko", "한국어"),
        LanguageOption("vi", "Tiếng Việt"),
        LanguageOption("tr", "Türkçe"),
        LanguageOption("id", "Bahasa Indonesia"),
        LanguageOption("ur", "اردو")
    )
}
