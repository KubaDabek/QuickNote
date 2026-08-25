package com.example.quicknote

import android.app.Application
import com.example.quicknote.ui.ThemeHelper

/**
 * Klasa Application, inicjalizująca globalne ustawienia aplikacji przy starcie procesu.
 */
class QuickNoteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Automatyczne zaaplikowanie zapisanego motywu (jasny/ciemny/systemowy)
        val themeMode = ThemeHelper.getThemePreference(this)
        ThemeHelper.applyTheme(themeMode)
    }
}
