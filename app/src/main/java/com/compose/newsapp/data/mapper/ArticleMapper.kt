package com.compose.newsapp.data.mapper

import com.compose.newsapp.data.local.entity.ArticleEntity
import com.compose.newsapp.data.remote.dto.ArticleDto
import com.compose.newsapp.domain.model.Article

// Maps ArticleDto (from API) to ArticleEntity (for Room)
// Returns null if essential data (like URL or title) is missing from the DTO
fun ArticleDto.toArticleEntity(): ArticleEntity? {
    // Validate essential fields from DTO
    val validUrl = this.url ?: return null // URL is primary key, cannot be null
    val validTitle = this.title ?: return null // Title is important display info
    val validPublishedAt = this.publishedAt ?: return null // Needed for sorting/display

    return ArticleEntity(
        url = validUrl,
        source = this.source?.toDomainSource()?.toEntitySourceEmbeddable(), // Map nested source
        author = this.author,
        title = validTitle,
        description = this.description,
        urlToImage = this.urlToImage,
        publishedAt = validPublishedAt,
        content = this.content,
        isBookmarked = false, // Default bookmark status for new articles from API
        insertedTimestamp = System.currentTimeMillis() // Record insertion time
    )
}

// Maps ArticleEntity (from Room) to Article (Domain)
fun ArticleEntity.toDomainArticle(): Article {
    return Article(
        id = this.url, // Use URL as domain ID
        source = this.source.toDomainSource(), // Map nested source
        author = this.author,
        title = this.title ?: "No Title", // Provide default if somehow null in DB
        description = this.description,
        url = this.url,
        urlToImage = this.urlToImage,
        publishedAt = this.publishedAt ?: "", // Provide default if somehow null
        content = this.content,
        isBookmarked = this.isBookmarked // Carry over bookmark status
    )
}

// Convenience function to map a list of entities
fun List<ArticleEntity>.toDomainArticleList(): List<Article> {
    return this.map { it.toDomainArticle() }
}

// Add this helper mapper (DTO -> Domain) inside data/mapper/ArticleMapper.kt
fun ArticleDto.toSimpleDomainArticle(): Article? {
    // Validate essential fields from DTO
    val validUrl = this.url ?: return null
    val validTitle = this.title ?: return null
    val validPublishedAt = this.publishedAt ?: return null

    return Article(
        id = validUrl,
        source = this.source.toDomainSource(),
        author = this.author,
        title = validTitle,
        description = this.description,
        url = validUrl,
        urlToImage = this.urlToImage,
        publishedAt = validPublishedAt,
        content = this.content,
        isBookmarked = false // Assume not bookmarked when coming directly from search API
    )
}