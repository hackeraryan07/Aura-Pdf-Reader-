package com.example.model

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(
        if (prefs.contains("is_dark_mode")) {
            prefs.getBoolean("is_dark_mode", false)
        } else {
            null // Signifies not set by user yet
        }
    )
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode

    private val _isPdfPreviewEnabled = MutableStateFlow(
        prefs.getBoolean("is_pdf_preview_enabled", true) // Default is true
    )
    val isPdfPreviewEnabled: StateFlow<Boolean> = _isPdfPreviewEnabled

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("is_dark_mode", isDark).apply()
        _isDarkMode.value = isDark
    }

    fun setPdfPreviewEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_pdf_preview_enabled", enabled).apply()
        _isPdfPreviewEnabled.value = enabled
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
