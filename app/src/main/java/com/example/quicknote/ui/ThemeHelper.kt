package com.example.quicknote.ui

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Obiekt zarządzający preferencjami i aplikowaniem motywu (jasny/ciemny/systemowy).
 */
object ThemeHelper {
    private const val PREFS_NAME = "quicknote_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    const val THEME_LIGHT = 0
    const val THEME_DARK = 1
    const val THEME_SYSTEM = 2

    /** Zmienia tryb nocny aplikacji na podstawie przekazanej wartości. */
    fun applyTheme(themeMode: Int) {
        when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    /** Odczytuje zapisany tryb motywu z SharedPreferences. */
    fun getThemePreference(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM)
    }

    /** Zapisuje wybrany przez użytkownika tryb motywu. */
    fun saveThemePreference(context: Context, themeMode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THEME_MODE, themeMode).apply()
    }
}
