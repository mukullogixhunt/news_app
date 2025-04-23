package com.compose.newsapp.data.local.entity

// Embeddable class for the Source object within ArticleEntity
data class SourceEmbeddable(
    val id: String?,
    val name: String? // Match DTO/Domain nullability expectations
)
