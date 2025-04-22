package com.compose.newsapp.domain.model

data class Article(
    val id: String, // Using URL as a unique ID if API doesn't provide one reliably
    val source: Source,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String, // Keep as ISO String for easier sorting/comparison in domain/data
    val content: String?,
    val isBookmarked: Boolean = false // Track bookmark status
)
