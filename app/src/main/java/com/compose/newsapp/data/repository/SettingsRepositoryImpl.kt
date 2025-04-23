package com.compose.newsapp.data.repository

import com.compose.newsapp.data.local.pref.PrefDataStore
import com.compose.newsapp.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val prefDataStore: PrefDataStore
) : SettingsRepository {
    override val themePreference: Flow<String>
        get() = prefDataStore.themePreference

    override suspend fun setThemePreference(theme: String) {
        prefDataStore.setThemePreference(theme)
    }

    override val syncIntervalPreference: Flow<Long>
        get() = prefDataStore.syncIntervalPreference

    override suspend fun setSyncIntervalPreference(intervalHours: Long) {
        prefDataStore.setSyncIntervalPreference(intervalHours)
    }


}