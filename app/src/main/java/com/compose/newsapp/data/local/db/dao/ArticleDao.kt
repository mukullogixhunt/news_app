package com.compose.newsapp.data.local.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.compose.newsapp.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    // Insert or update articles based on PrimaryKey (url)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<ArticleEntity>)

    // Get a PagingSource for all articles, ordered by insertion time (latest first)
    // Used by RemoteMediator flow
    @Query("SELECT * FROM articles ORDER BY insertedTimestamp DESC") // Or publishedAt DESC?
    fun getArticlesPagingSource(): PagingSource<Int, ArticleEntity>

    // Get a PagingSource specifically for bookmarked articles
    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY publishedAt DESC")
    fun getBookmarkedArticlesPagingSource(): PagingSource<Int, ArticleEntity>

    // Get a flow of bookmarked articles (if needed for non-paged display)
    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY publishedAt DESC")
    fun getBookmarkedArticlesFlow(): Flow<List<ArticleEntity>>

    // Update the bookmark status of a specific article
    @Query("UPDATE articles SET isBookmarked = :isBookmarked WHERE url = :articleUrl")
    suspend fun setBookmarkStatus(articleUrl: String, isBookmarked: Boolean)

    // Get a single article by URL (e.g., for detail view)
    @Query("SELECT * FROM articles WHERE url = :articleUrl")
    suspend fun getArticleByUrl(articleUrl: String): ArticleEntity?

    // Clear all articles (e.g., for RemoteMediator refresh)
    @Query("DELETE FROM articles")
    suspend fun clearAllArticles()

    // Count articles (useful for sync logic)
    @Query("SELECT COUNT(url) FROM articles")
    suspend fun countArticles(): Int

}