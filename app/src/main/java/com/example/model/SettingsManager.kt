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

    private val _aiProvider = MutableStateFlow(
        prefs.getString("ai_provider", "None (Disabled)") ?: "None (Disabled)"
    )
    val aiProvider: StateFlow<String> = _aiProvider

    private val _aiApiKey = MutableStateFlow(
        prefs.getString("ai_api_key", "") ?: ""
    )
    val aiApiKey: StateFlow<String> = _aiApiKey

    private val _aiModel = MutableStateFlow(
        prefs.getString("ai_model", "") ?: ""
    )
    val aiModel: StateFlow<String> = _aiModel

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("is_dark_mode", isDark).apply()
        _isDarkMode.value = isDark
    }

    fun setPdfPreviewEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_pdf_preview_enabled", enabled).apply()
        _isPdfPreviewEnabled.value = enabled
    }

    fun setAiProvider(provider: String) {
        prefs.edit().putString("ai_provider", provider).apply()
        _aiProvider.value = provider
    }

    fun setAiApiKey(key: String) {
        prefs.edit().putString("ai_api_key", key).apply()
        _aiApiKey.value = key
    }

    fun setAiModel(model: String) {
        prefs.edit().putString("ai_model", model).apply()
        _aiModel.value = model
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
