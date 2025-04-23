package com.compose.newsapp.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.newsapp.core.util.Logger
import com.compose.newsapp.domain.model.Article
import com.compose.newsapp.domain.usecase.article.GetArticleDetailsUseCase
import com.compose.newsapp.domain.usecase.article.SetBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Simple UI State Sealed Class
sealed interface ArticleDetailUiState {
    object Loading : ArticleDetailUiState
    data class Success(val article: Article) : ArticleDetailUiState
    data class Error(val message: String) : ArticleDetailUiState
}

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val getArticleDetailsUseCase: GetArticleDetailsUseCase,
    private val setBookmarkUseCase: SetBookmarkUseCase,
    savedStateHandle: SavedStateHandle // Hilt provides this
) : ViewModel() {
    // Get article URL from SavedStateHandle (passed via Nav Args)
    private val articleUrl: String = savedStateHandle.get<String>("articleUrl")
        ?: throw IllegalStateException("Article URL missing in arguments")

    private val _uiState = MutableStateFlow<ArticleDetailUiState>(ArticleDetailUiState.Loading)
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

    init {
        loadArticleDetails()
    }

    private fun loadArticleDetails() {
        _uiState.value = ArticleDetailUiState.Loading
        viewModelScope.launch {
            try {
                val article = getArticleDetailsUseCase(articleUrl)
                if (article != null) {
                    _uiState.value = ArticleDetailUiState.Success(article)
                } else {
                    _uiState.value = ArticleDetailUiState.Error("Article not found")
                }
            } catch (e: Exception) {
                Logger.e("ArticleDetailVM", "Error loading article", e)
                _uiState.value = ArticleDetailUiState.Error(e.localizedMessage ?: "Failed to load article")
            }
        }
    }

    fun toggleBookmark() {
        // Operate only on the Success state
        if (_uiState.value is ArticleDetailUiState.Success) {
            val currentArticle = (_uiState.value as ArticleDetailUiState.Success).article
            viewModelScope.launch {
                try {
                    setBookmarkUseCase(currentArticle.id, !currentArticle.isBookmarked)
                    // Re-fetch or update state optimistically
                    loadArticleDetails() // Simple re-fetch to get updated bookmark status
                } catch (e: Exception) {
                    Logger.e("ArticleDetailVM", "Error toggling bookmark", e)
                    // Optionally show error to user via another StateFlow<Event<String>>
                }
            }
        }
    }
}