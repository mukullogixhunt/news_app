package com.compose.newsapp.domain.model

data class Source(
    val id: String?, // Some sources might not have an ID from the API
    val name: String
)
