package com.compose.newsapp.presentation.headlines

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
import com.compose.newsapp.databinding.FragmentHeadlinesBinding
import com.compose.newsapp.domain.model.Article
import com.compose.newsapp.presentation.adapter.ArticleLoadStateAdapter
import com.compose.newsapp.presentation.adapter.ArticlePagingAdapter
import com.compose.newsapp.presentation.adapter.OnArticleInteractionListener
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HeadlinesFragment : Fragment(), OnArticleInteractionListener {

    // Use viewModels delegate provided by fragment-ktx
    private val viewModel: HeadlinesViewModel by viewModels()

    // ViewBinding Property Delegate (requires viewBinding delegate in core/ui)
    // Use '_binding' and 'binding' pattern for null safety
    private var _binding: FragmentHeadlinesBinding? = null
    private val binding get() = _binding!! // Non-null accessor

    private lateinit var articleAdapter: ArticlePagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHeadlinesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeHeadlines()
        observeLoadState() // Observe loading/error states AFTER setting up adapter

        binding.buttonRetry.setOnClickListener { articleAdapter.retry() }
    }

    private fun setupRecyclerView() {
        articleAdapter = ArticlePagingAdapter(this) // Pass fragment as listener
        binding.recyclerViewHeadlines.apply {
            adapter = articleAdapter.withLoadStateHeaderAndFooter(
                header = ArticleLoadStateAdapter { articleAdapter.retry() },
                footer = ArticleLoadStateAdapter { articleAdapter.retry() }
            )
            layoutManager = LinearLayoutManager(requireContext())
            // Optional: Add ItemDecoration for spacing
            // addItemDecoration(...)
        }
    }

    private fun observeHeadlines() {
        viewLifecycleOwner.lifecycleScope.launch {
            // repeatOnLifecycle ensures collection stops when view is destroyed
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.headlines.collectLatest { pagingData ->
                    // Submit new data to the adapter
                    articleAdapter.submitData(pagingData)
                }
            }
        }
    }

    private fun observeLoadState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                articleAdapter.loadStateFlow.collectLatest { loadStates ->
                    // Handle overall refresh state
                    binding.progressBarLoading.isVisible = loadStates.refresh is LoadState.Loading
                    binding.layoutError.isVisible = loadStates.refresh is LoadState.Error
                    binding.recyclerViewHeadlines.isVisible = loadStates.refresh !is LoadState.Error // Hide list on error

                    // Handle append state (loading more items) - you might want a footer progress bar instead
                    // binding.progressBarAppend.isVisible = loadStates.append is LoadState.Loading

                    // Display error message if refresh failed
                    val errorState = loadStates.refresh as? LoadState.Error
                        ?: loadStates.source.append as? LoadState.Error // Check append error too
                        ?: loadStates.source.prepend as? LoadState.Error
                    errorState?.let {
                        val errorMessage = it.error.localizedMessage ?: "An unknown error occurred"
                        binding.textViewErrorMessage.text = errorMessage
                        // Optionally show a Snackbar too
                        // Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
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
        // Navigate to detail fragment using Safe Args
        val action = HeadlinesFragmentDirections.actionHeadlinesToDetail(article.url)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewHeadlines.adapter = null // Clear adapter reference
        _binding = null // Avoid memory leaks
    }

}