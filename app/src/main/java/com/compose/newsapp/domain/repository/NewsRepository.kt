package com.compose.newsapp.domain.repository

import androidx.paging.PagingData
import com.compose.newsapp.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface NewsRepository {

    /**
     * Gets a stream of PagingData for top headlines, optionally filtered by country/category.
     * Handles fetching from network and caching locally.
     */
    fun getTopHeadlines(
        country: String, // e.g., "us", "gb"
        category: String? // e.g., "business", "technology"
    ): Flow<PagingData<Article>>

    /**
     * Gets a stream of PagingData for articles matching search criteria.
     * Primarily network-focused, potential for caching later.
     */
    fun searchArticles(
        query: String,
        sortBy: String // e.g., "publishedAt", "relevancy", "popularity"
    ): Flow<PagingData<Article>>


    /**
     * Gets a stream of PagingData for bookmarked articles from the local cache.
     */
    fun getBookmarkedArticles(): Flow<PagingData<Article>>

    /**
     * Marks an article as bookmarked or removes the bookmark.
     * @param articleId The unique ID (URL) of the article.
     * @param isBookmarked True to bookmark, false to remove bookmark.
     */
    suspend fun setBookmark(articleId: String, isBookmarked: Boolean)


    /**
     * Fetches the latest (e.g., first page) top headlines without paging.
     * Used by the background sync worker.
     * Returns the number of *new* articles fetched/updated.
     */
    suspend fun syncHeadlines(country: String, category: String?): Int

    suspend fun getArticle(url: String): Article? // Can return null if not found

}