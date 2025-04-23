package com.compose.newsapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey
    val articleUrl: String, // Links to the ArticleEntity's primary key
    val prevKey: Int?,
    val nextKey: Int?
)
