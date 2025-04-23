package com.compose.newsapp.data.remote.dto


// Matches the 'article' object in the NewsAPI response
data class ArticleDto(
    val source: SourceDto?,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?, // URL is crucial, should ideally not be null
    val urlToImage: String?,
    val publishedAt: String?, // ISO 8601 format
    val content: String?
)
