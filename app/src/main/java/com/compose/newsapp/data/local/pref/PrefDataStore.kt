package com.compose.newsapp.data.local.pref

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.compose.newsapp.core.util.Constants
import com.compose.newsapp.core.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Define Preference Keys (consider putting these in Constants or a dedicated Keys object)
private object PreferencesKeys {
    val THEME = stringPreferencesKey(Constants.PREF_KEY_THEME)
    val SYNC_INTERVAL = longPreferencesKey(Constants.PREF_KEY_SYNC_INTERVAL)
}

@Singleton
class PrefDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences> // Injected via AppModule
) {

    // --- Theme Preference ---
    val themePreference: Flow<String> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Logger.e("PrefDataStore", "Error reading theme preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferencesKeys.THEME] ?: Constants.THEME_SYSTEM // Default to system theme
        }

    suspend fun setThemePreference(theme: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme
        }
    }

    // --- Sync Interval Preference ---
    val syncIntervalPreference: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Logger.e("PrefDataStore", "Error reading sync interval preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferencesKeys.SYNC_INTERVAL] ?: Constants.DEFAULT_SYNC_INTERVAL_HOURS
        }

    suspend fun setSyncIntervalPreference(intervalHours: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SYNC_INTERVAL] = intervalHours
        }
    }
}