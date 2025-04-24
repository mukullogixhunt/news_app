package com.compose.newsapp.data.remote.api

import com.compose.newsapp.core.util.Constants
import com.compose.newsapp.data.remote.dto.NewsResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("apiKey") apiKey: String = Constants.NEWS_API_KEY,
        @Query("country") country: String,
        @Query("category") category: String? = null,
        @Query("pageSize") pageSize: Int = Constants.PAGE_SIZE,
        @Query("page") page: Int
    ): Response<NewsResponseDto> // Use Response for error handling

    @GET("v2/everything")
    suspend fun searchEverything(
        @Query("apiKey") apiKey: String = Constants.NEWS_API_KEY,
        @Query("q") query: String,
        @Query("sortBy") sortBy: String = "publishedAt", // Default sort
        @Query("pageSize") pageSize: Int = Constants.PAGE_SIZE,
        @Query("page") page: Int
    ): Response<NewsResponseDto>

}