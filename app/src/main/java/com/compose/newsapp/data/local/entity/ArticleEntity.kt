package com.compose.newsapp.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles") // Define table name
data class ArticleEntity(
    @PrimaryKey // URL is a good candidate for primary key
    val url: String,

    @Embedded(prefix = "source_") // Prefix to avoid column name clashes if needed
    val source: SourceEmbeddable?,

    val author: String?,
    val title: String?,
    val description: String?,
    val urlToImage: String?,
    val publishedAt: String?, // Store as ISO string from API
    val content: String?,
    val isBookmarked: Boolean = false, // Flag for bookmarking

    // Added for Paging with RemoteMediator - timestamp of when it was inserted/updated
    val insertedTimestamp: Long = System.currentTimeMillis()
)
