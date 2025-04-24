package com.compose.newsapp.domain.usecase.settings

import com.compose.newsapp.domain.repository.SettingsRepository
import javax.inject.Inject

class SetSyncIntervalUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(intervalHours: Long) {
        settingsRepository.setSyncIntervalPreference(intervalHours)
    }

}