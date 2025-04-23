package com.compose.newsapp.data.remote.dto

data class SourceDto(
    val id: String?, // Can be null
    val name: String? // Name can potentially be null in edge cases? API docs say string.
)
