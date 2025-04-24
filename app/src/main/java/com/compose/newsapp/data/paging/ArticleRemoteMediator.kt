package com.compose.newsapp.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.compose.newsapp.core.util.Logger
import com.compose.newsapp.data.local.db.AppDatabase
import com.compose.newsapp.data.local.entity.ArticleEntity
import com.compose.newsapp.data.local.entity.RemoteKeyEntity
import com.compose.newsapp.data.mapper.toArticleEntity
import com.compose.newsapp.data.remote.api.NewsApiService
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class ArticleRemoteMediator @Inject constructor(
    private val newsApiService: NewsApiService,
    private val appDatabase: AppDatabase,
    private val country: String, // Pass filtering parameters
    private val category: String?
) : RemoteMediator<Int, ArticleEntity>() {

    private val articleDao = appDatabase.articleDao()
    private val remoteKeyDao = appDatabase.remoteKeyDao()

    override suspend fun initialize(): InitializeAction {
        // Check if cache is empty or stale (e.g., based on time)
        // For simplicity, we'll always refresh if cache is empty, otherwise skip initial refresh
        // More sophisticated logic could check timestamps.
        return if (articleDao.countArticles() == 0) {
            InitializeAction.LAUNCH_INITIAL_REFRESH // Refresh if DB is empty
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH // Otherwise, assume cache is recent enough
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        val page: Int = when (loadType) {
            LoadType.REFRESH -> {
                // Get the closest remote key to the anchor position
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                // If no key, start from page 1. Otherwise, maybe use its nextKey -1?
                // Simpler: Refresh always starts from page 1
                1
            }
            LoadType.PREPEND -> {
                // Prepending not typically needed for news feeds (newest first)
                // Get the first item's remote key and use its prevKey
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKey = getRemoteKeyForLastItem(state)
                if (remoteKey?.nextKey == null) {
                    // If there's no remote key for the last item OR if the nextKey is null,
                    // it signifies the end of pagination.
                    Logger.d("RemoteMediator","APPEND: End of pagination reached (remoteKey=$remoteKey, nextKey=${remoteKey?.nextKey}).")
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                // Otherwise, use the nextKey to load the next page.
                Logger.d("RemoteMediator","APPEND: Requesting page: ${remoteKey.nextKey}")
                remoteKey.nextKey // Return the page number to load
            }
        }

        Logger.d("RemoteMediator","LoadType: $loadType, Page: $page")


        try {
            val apiResponse = newsApiService.getTopHeadlines(
                country = country,
                category = category,
                page = page,
                pageSize = state.config.pageSize // Use pageSize from PagingConfig
            )

            if (!apiResponse.isSuccessful) {
                throw HttpException(apiResponse) // Throw for non-2xx codes
            }

            val responseBody = apiResponse.body()
            val articles = responseBody?.articles ?: emptyList()
            val endOfPaginationReached = articles.isEmpty() // || (responseBody?.totalResults ?: 0) <= page * state.config.pageSize

            val articleEntities = articles.mapNotNull { it.toArticleEntity() } // Map DTOs to Entities

            Logger.d("RemoteMediator","Fetched ${articleEntities.size} articles for page $page")


            appDatabase.withTransaction { // Perform DB operations atomically
                if (loadType == LoadType.REFRESH) {
                    // Clear old data on refresh
                    remoteKeyDao.clearRemoteKeys()
                    articleDao.clearAllArticles()
                    Logger.d("RemoteMediator","Cleared DB on REFRESH")
                }

                // Calculate keys for previous and next pages
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1

                // Create RemoteKeyEntity objects for each article
                val keys = articleEntities.map {
                    RemoteKeyEntity(articleUrl = it.url, prevKey = prevKey, nextKey = nextKey)
                }

                // Insert new data
                remoteKeyDao.insertAll(keys)
                articleDao.insertAll(articleEntities)
                Logger.d("RemoteMediator","Inserted ${articleEntities.size} articles and ${keys.size} keys into DB")
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        } catch (exception: IOException) {
            Logger.e("RemoteMediator","IOException during load", exception)
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            Logger.e("RemoteMediator","HttpException during load: ${exception.code()}", exception)
            return MediatorResult.Error(exception)
        } catch (exception: Exception) {
            Logger.e("RemoteMediator","Generic Exception during load", exception)
            return MediatorResult.Error(exception)
        }
    }

    // Helper functions to get remote keys for PREPEND/APPEND/REFRESH logic
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ArticleEntity>): RemoteKeyEntity? {
        // Get the last page that was retrieved, not the last item.
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { article ->
                remoteKeyDao.getRemoteKeyByArticleUrl(article.url)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ArticleEntity>): RemoteKeyEntity? {
        // Get the first page that was retrieved, not the first item.
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { article ->
                remoteKeyDao.getRemoteKeyByArticleUrl(article.url)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, ArticleEntity>): RemoteKeyEntity? {
        // Get the item closest to the anchor position.
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.url?.let { url ->
                remoteKeyDao.getRemoteKeyByArticleUrl(url)
            }
        }
    }
}