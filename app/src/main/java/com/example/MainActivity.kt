package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.model.SettingsManager
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.AppTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsManager = SettingsManager.getInstance(this)
        setContent {
            val isDarkSystem = isSystemInDarkTheme()
            val isDarkMode by settingsManager.isDarkMode.collectAsState()
            
            AppTheme(darkTheme = isDarkMode ?: isDarkSystem) {
                MainAppScreen()
            }
        }
    }
}
