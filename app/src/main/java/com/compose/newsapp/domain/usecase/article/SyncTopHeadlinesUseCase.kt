package com.compose.newsapp.domain.usecase.article

import com.compose.newsapp.domain.repository.NewsRepository
import javax.inject.Inject

class SyncTopHeadlinesUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    suspend fun execute(country: String = "us", category: String? = null): Int { // Default values
        return newsRepository.syncHeadlines(country, category)
    }
}