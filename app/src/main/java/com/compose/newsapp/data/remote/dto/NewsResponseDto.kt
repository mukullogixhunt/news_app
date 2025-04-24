package com.compose.newsapp.data.remote.dto

// Matches the top-level NewsAPI response structure
data class NewsResponseDto(
    val status: String, // "ok" or "error"
    val totalResults: Int?,
    val articles: List<ArticleDto>?,
    // Fields for error responses
    val code: String?,
    val message: String?
)
