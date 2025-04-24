package com.compose.newsapp.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.compose.newsapp.domain.model.Article
import com.compose.newsapp.domain.usecase.article.SearchArticlesUseCase
import com.compose.newsapp.domain.usecase.article.SetBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SEARCH_QUERY_KEY = "searchQuery"
private const val SEARCH_DEBOUNCE_MS = 500L // Debounce time for search input

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchArticlesUseCase: SearchArticlesUseCase,
    private val setBookmarkUseCase: SetBookmarkUseCase,
    private val savedStateHandle: SavedStateHandle // To save/restore query
) : ViewModel() {

    // Use SavedStateHandle to store and retrieve the query
    val searchQuery = savedStateHandle.getStateFlow(SEARCH_QUERY_KEY, "")

    // StateFlow for the actual search trigger (debounced)
    private val _searchTrigger = MutableStateFlow(searchQuery.value)

    val searchResults: Flow<PagingData<Article>> = _searchTrigger
        .debounce(SEARCH_DEBOUNCE_MS) // Apply debounce
        .filter { query -> query.length > 2 || query.isEmpty() } // Trigger only if query is long enough or cleared
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                // Return empty flow if query is cleared
                flowOf(PagingData.empty())
            } else {
                // Default sort by relevance for search
                searchArticlesUseCase(query = query, sortBy = "relevancy")
            }
        }
        .cachedIn(viewModelScope) // Cache results in ViewModel

    fun onQueryChanged(query: String) {
        // Update SavedStateHandle which propagates to searchQuery StateFlow
        savedStateHandle[SEARCH_QUERY_KEY] = query
        // Also update the trigger immediately for debounce logic
        _searchTrigger.value = query
    }

    // Function to trigger search immediately (e.g., on button press or IME action)
    fun performSearchNow() {
        // Update trigger with current query value to bypass debounce if needed,
        // or just ensure the latest value is emitted if debounce is active.
        _searchTrigger.value = searchQuery.value
    }

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            // Toggle the bookmark status
            setBookmarkUseCase(article.id, !article.isBookmarked)
            // TODO: Ideally, the UI should update optimistically or observe changes
            // from a single source of truth (DB). Paging doesn't automatically refresh
            // the current list on DB changes unless using RemoteMediator's DB source.
            // For search (network only), this won't reflect immediately in the list.
            // A manual refresh might be needed, or observe the bookmark status separately.
        }
    }
}