package com.compose.newsapp.core.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel : ViewModel() {


    // Example: Common Loading State
    protected val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Example: Common Error State (can be more specific)
    protected val _error = MutableStateFlow<String?>(null) // Holds error message
    val error: StateFlow<String?> = _error.asStateFlow()

    // Function to clear error state once handled by UI
    fun clearError() {
        _error.value = null
    }

}