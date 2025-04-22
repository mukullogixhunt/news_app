package com.compose.newsapp.domain.usecase.settings

import com.compose.newsapp.domain.repository.SettingsRepository
import javax.inject.Inject

class SetThemeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(theme: String) {
        settingsRepository.setThemePreference(theme)
    }

}