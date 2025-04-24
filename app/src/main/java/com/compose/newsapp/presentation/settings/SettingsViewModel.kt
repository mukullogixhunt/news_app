package com.compose.newsapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.newsapp.core.util.Constants
import com.compose.newsapp.domain.usecase.settings.GetSyncIntervalUseCase
import com.compose.newsapp.domain.usecase.settings.GetThemeUseCase
import com.compose.newsapp.domain.usecase.settings.SetSyncIntervalUseCase
import com.compose.newsapp.domain.usecase.settings.SetThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getThemeUseCase: GetThemeUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val getSyncIntervalUseCase: GetSyncIntervalUseCase,
    private val setSyncIntervalUseCase: SetSyncIntervalUseCase
    // Inject other settings use cases if needed
) : ViewModel() {


    // StateFlow for theme preference
    val themeState: StateFlow<String> = getThemeUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L), // Keep active 5s after last subscriber
            initialValue = Constants.THEME_SYSTEM // Sensible initial value
        )

    // StateFlow for sync interval preference
    val syncIntervalState: StateFlow<Long> = getSyncIntervalUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = Constants.DEFAULT_SYNC_INTERVAL_HOURS
        )


    fun setTheme(theme: String) {
        viewModelScope.launch {
            setThemeUseCase(theme)
        }
    }

    fun setSyncInterval(intervalHours: Long) {
        viewModelScope.launch {
            setSyncIntervalUseCase(intervalHours)
        }
    }
    // Add functions to handle other settings changes

}