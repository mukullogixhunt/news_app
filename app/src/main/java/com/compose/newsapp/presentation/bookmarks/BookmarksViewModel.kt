package com.compose.newsapp.presentation.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.compose.newsapp.domain.model.Article
import com.compose.newsapp.domain.usecase.article.GetBookmarkedArticlesUseCase
import com.compose.newsapp.domain.usecase.article.SetBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val getBookmarkedArticlesUseCase: GetBookmarkedArticlesUseCase,
    private val setBookmarkUseCase: SetBookmarkUseCase
) : ViewModel(){

    val bookmarkedArticles: Flow<PagingData<Article>> =
        getBookmarkedArticlesUseCase() // Execute the use case
            .cachedIn(viewModelScope) // Cache the results

    // Bookmarks are inherently toggleable (remove from this screen)
    fun removeBookmark(article: Article) {
        viewModelScope.launch {
            setBookmarkUseCase(article.id, false) // Always set to false here
            // PagingDataAdapter should update automatically as it observes the DB source
        }
    }

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            setBookmarkUseCase(article.id, !article.isBookmarked)
            // Refresh might be needed if not using DB source directly
        }
    }

}