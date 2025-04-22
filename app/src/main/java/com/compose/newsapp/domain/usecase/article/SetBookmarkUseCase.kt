package com.compose.newsapp.domain.usecase.article

import com.compose.newsapp.domain.repository.NewsRepository
import javax.inject.Inject

class SetBookmarkUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(articleId: String, isBookmarked: Boolean) {
        newsRepository.setBookmark(articleId, isBookmarked)
    }
}