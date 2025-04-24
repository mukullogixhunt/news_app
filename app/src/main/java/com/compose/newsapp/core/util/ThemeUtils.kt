package com.compose.newsapp.core.util

import androidx.appcompat.app.AppCompatDelegate

object ThemeUtils {

    fun applyTheme(themePref: String) {
        when (themePref) {
            Constants.THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Constants.THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) // Default to system
        }
    }
}