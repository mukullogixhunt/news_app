package com.compose.newsapp.domain.usecase.article

import androidx.paging.PagingData
import com.compose.newsapp.domain.model.Article
import com.compose.newsapp.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTopHeadlinesUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {

    operator fun invoke(country: String, category: String?): Flow<PagingData<Article>> {
        // Add validation or default values if needed
        return newsRepository.getTopHeadlines(country, category)
    }

}