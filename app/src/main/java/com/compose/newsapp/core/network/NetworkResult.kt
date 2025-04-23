package com.compose.newsapp.core.network

import com.compose.newsapp.core.util.Logger

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int? = null, val message: String?) : NetworkResult<Nothing>() // Add code
    object Loading : NetworkResult<Nothing>()
}

// Simple wrapper for calls expecting a specific success body (like Retrofit calls)
suspend fun <T: Any> safeApiCall( // Changed to Any to avoid nullable T?
    errorMessagePrefix: String = "API Call Failed",
    apiCall: suspend () -> retrofit2.Response<T> // Expect Retrofit Response
): NetworkResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                // Successful response but empty body - might be an error depending on API
                Logger.w("safeApiCall", "$errorMessagePrefix: Response body is null. Code: ${response.code()}")
                NetworkResult.Error(response.code(), "Response body is null")
            }
        } else {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            Logger.e("safeApiCall", "$errorMessagePrefix: Code: ${response.code()}, Message: ${response.message()}, ErrorBody: $errorBody")
            NetworkResult.Error(response.code(), "${response.message()} - $errorBody")
        }
    } catch (e: Exception) {
        Logger.e("safeApiCall", "$errorMessagePrefix: Exception: ${e.message}", e)
        NetworkResult.Error(null, e.message ?: "An unexpected error occurred")
    }
}