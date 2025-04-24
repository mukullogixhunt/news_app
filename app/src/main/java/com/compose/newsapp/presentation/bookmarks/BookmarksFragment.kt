package com.compose.newsapp.presentation.bookmarks

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.compose.newsapp.databinding.FragmentBookmarksBinding
import com.compose.newsapp.domain.model.Article
import com.compose.newsapp.presentation.adapter.ArticlePagingAdapter
import com.compose.newsapp.presentation.adapter.OnArticleInteractionListener
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookmarksFragment : Fragment(), OnArticleInteractionListener  {

    private val viewModel: BookmarksViewModel by viewModels()
    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!

    private lateinit var articleAdapter: ArticlePagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeBookmarks()
        observeLoadState()
    }

    private fun setupRecyclerView() {
        articleAdapter = ArticlePagingAdapter(this) // Pass listener
        binding.recyclerViewBookmarks.apply {
            adapter = articleAdapter
            // Don't need load state footer for bookmarks (no network append)
            // If you want a general loading indicator, handle loadStateFlow directly
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeBookmarks() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bookmarkedArticles.collectLatest { pagingData ->
                    articleAdapter.submitData(pagingData)
                }
            }
        }
    }

    private fun observeLoadState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                articleAdapter.loadStateFlow.collectLatest { loadStates ->
                    // Initial load from DB
                    binding.progressBarBookmarksLoading.isVisible = loadStates.refresh is LoadState.Loading
                    binding.textViewNoBookmarks.isVisible = loadStates.refresh is LoadState.NotLoading && articleAdapter.itemCount == 0
                    binding.recyclerViewBookmarks.isVisible = !(loadStates.refresh is LoadState.Error && articleAdapter.itemCount == 0) // Could show error text

                    // Can check for DB errors here if needed
                    val errorState = loadStates.refresh as? LoadState.Error
                    if (errorState != null) {
                        // Handle error state (e.g., show error message)
                        binding.textViewNoBookmarks.text = "Error loading bookmarks."
                        binding.textViewNoBookmarks.visibility = View.VISIBLE
                    }
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

    // Implementation of the click listener
    override fun onArticleClick(article: Article) {
        val action = BookmarksFragmentDirections.actionBookmarksToDetail(article.url)
        findNavController().navigate(action)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewBookmarks.adapter = null
        _binding = null
    }
}