package com.compose.newsapp.presentation.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.compose.newsapp.R

import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI // For toolbar navigation
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar

import com.compose.newsapp.core.util.DateTimeUtils
import com.compose.newsapp.databinding.FragmentArticleDetailBinding
import com.compose.newsapp.domain.model.Article
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.net.toUri


@AndroidEntryPoint
class ArticleDetailFragment : Fragment() {

    private var _binding: FragmentArticleDetailBinding? = null
    private val binding get() = _binding!!
    private val args: ArticleDetailFragmentArgs by navArgs()
    private val viewModel: ArticleDetailViewModel by viewModels()

    private var currentArticle: Article? = null // To hold article for Share action



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupMenu() // Setup Options Menu

        binding.fabBookmark.setOnClickListener { viewModel.toggleBookmark() }
        binding.buttonViewOnline.setOnClickListener { openArticleInBrowser() }

        observeUiState()
    }

    private fun setupToolbar() {
        // Set the toolbar for the fragment
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarDetail)
        // Enable up navigation using the NavController
        NavigationUI.setupWithNavController(binding.toolbarDetail, findNavController())
        // Hide the title initially, CollapsingToolbarLayout handles it
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    // --- Options Menu (for Share) ---
    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.article_detail_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_share -> {
                        shareArticle()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED) // Use viewLifecycleOwner
    }

    private fun shareArticle() {
        currentArticle?.let { article ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, article.title) // Subject for email apps
                putExtra(Intent.EXTRA_TEXT, article.url) // Share the URL
            }
            try {
                startActivity(Intent.createChooser(shareIntent, "Share Article via"))
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Cannot share article.", Snackbar.LENGTH_SHORT).show()
            }
        } ?: run {
            Snackbar.make(binding.root, "Article not loaded yet.", Snackbar.LENGTH_SHORT).show()
        }
    }
    // --- End Options Menu ---


    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    binding.progressBarDetailLoading.isVisible = state is ArticleDetailUiState.Loading
                    binding.textViewDetailError.isVisible = state is ArticleDetailUiState.Error

                    if (state is ArticleDetailUiState.Success) {
                        bindArticleData(state.article)
                        currentArticle = state.article // Store for share action
                    } else if (state is ArticleDetailUiState.Error) {
                        binding.textViewDetailError.text = state.message
                    }

                    // Hide content views if loading or error
                    val contentVisible = state is ArticleDetailUiState.Success
                    binding.collapsingToolbarLayout.isVisible = contentVisible
                    binding.textViewDetailTitle.isVisible = contentVisible
                    binding.textViewDetailMeta.isVisible = contentVisible
                    binding.textViewDetailContent.isVisible = contentVisible
                    binding.buttonViewOnline.isVisible = contentVisible
                    binding.fabBookmark.isVisible = contentVisible // Show FAB only when loaded
                }
            }
        }
    }

    private fun bindArticleData(article: Article) {
        binding.collapsingToolbarLayout.title = article.source.name // Show source in collapsed toolbar
        binding.textViewDetailTitle.text = article.title
        binding.textViewDetailMeta.text = "${article.source.name} - ${DateTimeUtils.formatIsoDateTime(article.publishedAt)}"
        binding.textViewDetailContent.text = article.content ?: article.description ?: "No content available." // Fallback content

        // Load backdrop image
        Glide.with(this)
            .load(article.urlToImage)
            .placeholder(R.drawable.ic_placeholder_image)
            .error(R.drawable.ic_placeholder_image)
            .into(binding.imageViewDetailBackdrop)

        // Update FAB icon based on bookmark state
        val bookmarkIconRes = if (article.isBookmarked) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border
        binding.fabBookmark.setImageDrawable(ContextCompat.getDrawable(requireContext(), bookmarkIconRes))
    }

    private fun openArticleInBrowser() {
        currentArticle?.url?.let { url ->
            try {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                startActivity(intent)
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Cannot open link", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the toolbar set by the fragment when the view is destroyed
        (activity as? AppCompatActivity)?.setSupportActionBar(null)
        _binding = null
        currentArticle = null
    }
}