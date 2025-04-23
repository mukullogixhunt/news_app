package com.compose.newsapp.presentation.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.compose.newsapp.core.extension.hideKeyboard
import com.compose.newsapp.databinding.FragmentSearchBinding
import com.compose.newsapp.domain.model.Article
import com.compose.newsapp.presentation.adapter.ArticleLoadStateAdapter
import com.compose.newsapp.presentation.adapter.ArticlePagingAdapter
import com.compose.newsapp.presentation.adapter.OnArticleInteractionListener
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment(), OnArticleInteractionListener {


    private val viewModel: SearchViewModel by viewModels()
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var articleAdapter: ArticlePagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchInput()
        observeSearchResults()
        observeLoadState()
        observeQueryChanges() // Keep EditText synced with ViewModel state

        binding.buttonSearchRetry.setOnClickListener { articleAdapter.retry() }
        binding.buttonSearch.setOnClickListener { triggerSearch() }
    }

    private fun setupRecyclerView() {
        articleAdapter = ArticlePagingAdapter(this)
        binding.recyclerViewSearchResults.apply {
            adapter = articleAdapter.withLoadStateHeaderAndFooter(
                header = ArticleLoadStateAdapter { articleAdapter.retry() },
                footer = ArticleLoadStateAdapter { articleAdapter.retry() }
            )
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSearchInput() {
        // Update ViewModel when text changes (debounced in VM)
        binding.editTextSearch.doAfterTextChanged { text ->
            viewModel.onQueryChanged(text.toString())
        }

        // Trigger search on "Search" IME action
        binding.editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                triggerSearch()
                true
            } else {
                false
            }
        }
    }

    // Keep EditText synced with ViewModel state (e.g., on process death restoration)
    private fun observeQueryChanges() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchQuery.collectLatest { query ->
                    // Prevent infinite loop if text change triggers observation
                    if (binding.editTextSearch.text.toString() != query) {
                        binding.editTextSearch.setText(query)
                        // Move cursor to the end after setting text programmatically
                        binding.editTextSearch.setSelection(query.length)
                    }
                }
            }
        }
    }

    private fun triggerSearch() {
        viewModel.performSearchNow()
         hideKeyboard() // Hide keyboard after explicit search action
    }


    private fun observeSearchResults() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchResults.collectLatest { pagingData ->
                    articleAdapter.submitData(pagingData)
                }
            }
        }
    }

    private fun observeLoadState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                articleAdapter.loadStateFlow
                    // Use loadStateFlow to get the states for the main list (source)
                    // and the refresh state separately.
                    .collectLatest { loadStates ->
                        val refreshState = loadStates.refresh
                        val sourceLoadState = loadStates.source

                        // Initial load/refresh state
                        binding.progressBarSearchLoading.isVisible = refreshState is LoadState.Loading
                        binding.layoutSearchError.isVisible = refreshState is LoadState.Error
                        binding.textViewSearchPrompt.isVisible = refreshState is LoadState.NotLoading && articleAdapter.itemCount == 0 && viewModel.searchQuery.value.isEmpty()
                        binding.recyclerViewSearchResults.isVisible = !(refreshState is LoadState.Error && articleAdapter.itemCount == 0) // Hide list only on initial error

                        // Show prompt if refresh is successful but list is empty AND query is present
                        val isEmptyList = refreshState is LoadState.NotLoading && articleAdapter.itemCount == 0 && viewModel.searchQuery.value.isNotEmpty()
                        if(isEmptyList) {
                            binding.textViewSearchPrompt.text = "No results found for '${viewModel.searchQuery.value}'"
                            binding.textViewSearchPrompt.visibility = View.VISIBLE
                        } else if (viewModel.searchQuery.value.isEmpty()) {
                            binding.textViewSearchPrompt.text = "Enter a keyword to search news"
                            // Visibility handled above
                        } else {
                            binding.textViewSearchPrompt.visibility = View.GONE
                        }


                        // Handle error message for refresh
                        if (refreshState is LoadState.Error) {
                            binding.textViewSearchErrorMessage.text = refreshState.error.localizedMessage ?: "Unknown Error"
                        }

                        // Append/Prepend errors can be shown in the LoadStateAdapter footer/header
                    }
            }
        }
    }

    // Implement the new listener method
    override fun onBookmarkToggle(article: Article) {
        viewModel.toggleBookmark(article) // Call ViewModel method
        // Optional: Show Snackbar confirmation
        val message = if (!article.isBookmarked) "Article bookmarked" else "Bookmark removed"
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    // Implementation of the click listener from ArticlePagingAdapter
    override fun onArticleClick(article: Article) {
        // Navigate to detail fragment using Safe Args
        val action = SearchFragmentDirections.actionSearchToDetail(article.url)
        findNavController().navigate(action)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewSearchResults.adapter = null
        _binding = null
    }
}