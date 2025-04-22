package com.compose.newsapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    val themePreference: Flow<String> // Emits theme string ("system", "light", "dark")
    suspend fun setThemePreference(theme: String)

    val syncIntervalPreference: Flow<Long> // Emits interval in hours
    suspend fun setSyncIntervalPreference(intervalHours: Long)

}