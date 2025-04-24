package com.compose.newsapp.data.remote.interceptor

import com.compose.newsapp.core.util.Constants
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            // Add the API key header to every request
            .header("X-Api-Key", Constants.NEWS_API_KEY)
            .build()
        return chain.proceed(newRequest)
    }
}