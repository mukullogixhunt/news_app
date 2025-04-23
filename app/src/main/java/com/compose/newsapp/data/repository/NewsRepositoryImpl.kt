package com.compose.newsapp.data.repository

import androidx.paging.*
import androidx.room.withTransaction
import com.compose.newsapp.core.util.Constants
import com.compose.newsapp.core.util.Logger
import com.compose.newsapp.data.local.db.AppDatabase
import com.compose.newsapp.data.local.db.dao.ArticleDao
import com.compose.newsapp.data.mapper.toArticleEntity
import com.compose.newsapp.data.mapper.toDomainArticle
import com.compose.newsapp.data.paging.ArticleRemoteMediator
import com.compose.newsapp.data.paging.SearchPagingSource
import com.compose.newsapp.data.remote.api.NewsApiService
import com.compose.newsapp.domain.model.Article
import com.compose.newsapp.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

class NewsRepositoryImpl @Inject constructor(
    private val newsApiService: NewsApiService,
    private val appDatabase: AppDatabase,
    // We need the DAO directly for some operations like bookmarking
    private val articleDao: ArticleDao
) : NewsRepository {


    @OptIn(ExperimentalPagingApi::class)
    override fun getTopHeadlines(country: String, category: String?): Flow<PagingData<Article>> {

        Logger.d(
            "NewsRepositoryImpl",
            "getTopHeadlines called: country=$country, category=$category"
        )
        // Define the function that returns the PagingSource from the Database
        val pagingSourceFactory = { articleDao.getArticlesPagingSource() }

        return Pager(
            config = PagingConfig(
                pageSize = Constants.PAGE_SIZE,
                enablePlaceholders = false // Or true if you want placeholders
                // prefetchDistance: Adjust if needed
            ),
            // Create an instance of the RemoteMediator
            remoteMediator = ArticleRemoteMediator(
                newsApiService = newsApiService,
                appDatabase = appDatabase,
                country = country,
                category = category
            ),
            // Provide the PagingSource factory from the local DB
            pagingSourceFactory = pagingSourceFactory
        ).flow
            // Map the Flow<PagingData<ArticleEntity>> to Flow<PagingData<Article>>
            .map { pagingDataEntity ->
                pagingDataEntity.map { articleEntity ->
                    articleEntity.toDomainArticle()
                }
            }
    }

    override fun searchArticles(query: String, sortBy: String): Flow<PagingData<Article>> {

        Logger.d("NewsRepositoryImpl", "searchArticles called: query=$query, sortBy=$sortBy")

        return Pager(
            config = PagingConfig(
                pageSize = Constants.PAGE_SIZE,
                enablePlaceholders = false
            ),
            // Use the network-only PagingSource for search
            pagingSourceFactory = {
                SearchPagingSource(
                    newsApiService = newsApiService,
                    query = query,
                    sortBy = sortBy
                )
            }
        ).flow // Flow<PagingData<Article>> is already returned by SearchPagingSource
    }

    override fun getBookmarkedArticles(): Flow<PagingData<Article>> {
        Logger.d("NewsRepositoryImpl", "getBookmarkedArticles called")
        val pagingSourceFactory = { articleDao.getBookmarkedArticlesPagingSource() }
        return Pager(
            config = PagingConfig(
                pageSize = Constants.PAGE_SIZE, // Or a different size for bookmarks?
                enablePlaceholders = false
            ),
            // No remote mediator needed, just read from DB
            pagingSourceFactory = pagingSourceFactory
        ).flow
            .map { pagingDataEntity ->
                pagingDataEntity.map { articleEntity ->
                    articleEntity.toDomainArticle()
                }
            }
    }

    override suspend fun setBookmark(articleId: String, isBookmarked: Boolean) {
        Logger.d(
            "NewsRepositoryImpl",
            "setBookmark called: articleId=$articleId, isBookmarked=$isBookmarked"
        )
        // Update the bookmark status directly in the database
        articleDao.setBookmarkStatus(articleUrl = articleId, isBookmarked = isBookmarked)
    }

    override suspend fun syncHeadlines(country: String, category: String?): Int {
        Logger.d("NewsRepositoryImpl","syncHeadlines called: country=$country, category=$category")
        var newArticlesCount = 0
        try {
            // Fetch only the first page for sync
            val response = newsApiService.getTopHeadlines(
                country = country,
                category = category,
                page = 1,
                pageSize = Constants.PAGE_SIZE // Fetch a standard page size
            )

            if (response.isSuccessful) {
                val articlesDto = response.body()?.articles ?: emptyList()
                val articleEntities = articlesDto.mapNotNull { it.toArticleEntity() }

                if (articleEntities.isNotEmpty()) {
                    appDatabase.withTransaction {
                        // Insert/Replace fetched articles. Room handles conflicts.
                        articleDao.insertAll(articleEntities)
                        // How to count *new*? Could compare timestamps or check existing before insert.
                        // Simplification: Assume any insert counts. More accurately, check if REPLACE happened.
                        // Room's insert returns List<Long> of rowIds. New inserts are > 0.
                        // However, REPLACE might return rowId of replaced item.
                        // More robust: Query existing URLs before insert.
                        // Let's keep it simple and return fetched count for now.
                        newArticlesCount = articleEntities.size
                        Logger.d("NewsRepositoryImpl","Sync: Inserted/Replaced ${articleEntities.size} articles.")
                    }
                } else {
                    Logger.d("NewsRepositoryImpl","Sync: No articles returned from API.")
                }
            } else {
                Logger.w("NewsRepositoryImpl","Sync failed: API Error ${response.code()}")
                // Optionally throw exception or handle specific error codes
            }

        } catch (e: IOException) {
            Logger.e("NewsRepositoryImpl", "Sync failed: IOException", e)
            // Optionally rethrow or return error code
        } catch (e: HttpException) {
            Logger.e("NewsRepositoryImpl", "Sync failed: HttpException ${e.code()}", e)
        } catch (e: Exception) {
            Logger.e("NewsRepositoryImpl", "Sync failed: Generic Exception", e)
        }
        return newArticlesCount
    }

    override suspend fun getArticle(url: String): Article? {
        // Fetch directly from DAO
        val entity = articleDao.getArticleByUrl(url)
        return entity?.toDomainArticle()
    }
}