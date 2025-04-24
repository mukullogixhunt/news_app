package com.compose.newsapp.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.compose.newsapp.core.util.Logger
import com.compose.newsapp.data.mapper.toSimpleDomainArticle
import com.compose.newsapp.data.remote.api.NewsApiService
import com.compose.newsapp.data.remote.dto.ArticleDto
import com.compose.newsapp.domain.model.Article
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SearchPagingSource @Inject constructor(
    private val newsApiService: NewsApiService,
    private val query: String,
    private val sortBy: String
) : PagingSource<Int, Article>() {
    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        // Try to find the page key of the closest page to anchorPosition, from
        // either the prevKey or the nextKey, but you need to handle nullability
        // appropriately:
        // * prevKey == null -> anchorPage is the first page.
        // * nextKey == null -> anchorPage is the last page.
        // * both prevKey and nextKey null -> anchorPage is the initial page, so
        //   just return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: 1 // Start page index is 1 for NewsAPI

        return try {
            Logger.d(
                "SearchPagingSource",
                "Loading page: $page for query: '$query'"
            )
            val response = newsApiService.searchEverything(
                query = query,
                sortBy = sortBy,
                page = page,
                pageSize = params.loadSize
            )

            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            val responseBody = response.body()
            val articlesDto: List<ArticleDto> = responseBody?.articles ?: emptyList()

            // Map DTOs directly to Domain models, filtering out invalid ones
            val articlesDomain: List<Article> = articlesDto
                .filter { it.url != null && it.title != null && it.publishedAt != null } // Basic validation
                .mapNotNull { dto ->
                    // A simplified mapping might be needed here if we don't need the bookmark status
                    // from the DB for search results. Let's assume we map fully for consistency.
                    // This requires a way to map DTO -> Domain directly, or DTO -> Entity -> Domain.
                    // Let's create a direct DTO -> Domain mapper for this.
                    dto.toSimpleDomainArticle() // Need to define this mapper
                }

            Logger.d(
                "SearchPagingSource",
                "Loaded ${articlesDomain.size} articles for page $page"
            )


            val nextKey = if (articlesDomain.isEmpty() || (responseBody?.totalResults
                    ?: 0) <= page * params.loadSize
            ) {
                null // End of pagination
            } else {
                page + 1
            }
            val prevKey = if (page == 1) null else page - 1

            LoadResult.Page(
                data = articlesDomain,
                prevKey = prevKey,
                nextKey = nextKey
            )

        } catch (exception: IOException) {
            Logger.e(
                "SearchPagingSource",
                "IOException during load",
                exception
            )
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            Logger.e(
                "SearchPagingSource",
                "HttpException during load: ${exception.code()}",
                exception
            )
            // Specific API error handling (e.g., rate limit) could go here
            if (exception.code() == 426) { // Example: Upgrade required - treat as end?
                return LoadResult.Page(emptyList(), null, null) // Pretend end of list
            }
            return LoadResult.Error(exception)
        } catch (exception: Exception) {
            Logger.e(
                "SearchPagingSource",
                "Generic Exception during load",
                exception
            )
            return LoadResult.Error(exception)
        }
    }
}