package com.alarmclock.app.settings

import android.content.Context
import android.os.Build
import java.util.Locale

/**
 * Manual per-app locale override that works for any Context (Application, Activity, Service),
 * not just AppCompatActivity. The chosen language code is persisted in plain SharedPreferences
 * (not DataStore) so it can be read synchronously from attachBaseContext, before Compose/coroutines
 * are available.
 */
object LocaleHelper {
    private const val PREFS = "locale_prefs"
    private const val KEY_LANGUAGE = "language_code"

    fun getLanguageCode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun setLanguageCode(context: Context, code: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, code)
            .apply()
    }

    /** Wrap [base] with a Configuration pinned to the persisted language, for use in attachBaseContext. */
    fun wrapContext(base: Context): Context {
        val code = getLanguageCode(base)
        return applyLocaleToContext(base, code)
    }

    fun applyLocaleToContext(base: Context, code: String): Context {
        val locale = Locale(code)
        Locale.setDefault(locale)
        val config = base.resources.configuration
        config.setLocale(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = android.os.LocaleList(locale)
            android.os.LocaleList.setDefault(localeList)
            config.setLocales(localeList)
        }
        return base.createConfigurationContext(config)
    }
}
