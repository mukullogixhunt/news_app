package com.compose.newsapp.presentation.headlines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.compose.newsapp.domain.model.Article
import com.compose.newsapp.domain.usecase.article.GetTopHeadlinesUseCase
import com.compose.newsapp.domain.usecase.article.SetBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeadlinesViewModel @Inject constructor(
    private val getTopHeadlinesUseCase: GetTopHeadlinesUseCase,
    private val setBookmarkUseCase: SetBookmarkUseCase
    // TODO: Inject settings repository/usecase to get default country/category
) : ViewModel() {


    // TODO: Use StateFlow for country/category selection from settings/UI
    private val _currentCountry = MutableStateFlow("us") // Default to US for now
    private val _currentCategory = MutableStateFlow<String?>(null) // Default to all categories

    @OptIn(ExperimentalCoroutinesApi::class)
    val headlines: Flow<PagingData<Article>> = _currentCountry
        // Combine with category if needed, or use flatMapLatest directly on country
        // For now, just reacting to country change
        .flatMapLatest { country ->
            // Fetch headlines based on the current country
            getTopHeadlinesUseCase(country = country, category = _currentCategory.value)
        }
        .cachedIn(viewModelScope) // Cache the PagingData in ViewModel scope

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            setBookmarkUseCase(article.id, !article.isBookmarked)
            // Refresh might be needed if not using DB source directly
        }
    }

    // --- Future methods ---
    // fun setCountry(countryCode: String) { _currentCountry.value = countryCode }
    // fun setCategory(category: String?) { _currentCategory.value = category }
}