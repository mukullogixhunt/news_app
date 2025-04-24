package com.compose.newsapp.domain.usecase.article

import com.compose.newsapp.domain.model.Article
import com.compose.newsapp.domain.repository.NewsRepository
import javax.inject.Inject

class GetArticleDetailsUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(url: String): Article? {
        return newsRepository.getArticle(url)
    }
}