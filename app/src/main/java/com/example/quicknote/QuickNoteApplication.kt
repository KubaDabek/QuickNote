package com.example.quicknote

import android.app.Application
import com.example.quicknote.ui.ThemeHelper

class QuickNoteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val themeMode = ThemeHelper.getThemePreference(this)
        ThemeHelper.applyTheme(themeMode)
    }
}
