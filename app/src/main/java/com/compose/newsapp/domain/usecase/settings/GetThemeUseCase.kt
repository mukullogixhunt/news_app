package com.compose.newsapp.domain.usecase.settings

import com.compose.newsapp.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetThemeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    operator fun invoke(): Flow<String> {
        return settingsRepository.themePreference
    }

}