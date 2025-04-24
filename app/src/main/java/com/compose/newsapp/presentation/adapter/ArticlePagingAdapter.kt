package com.compose.newsapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.compose.newsapp.R
import com.compose.newsapp.core.util.DateTimeUtils
import com.compose.newsapp.databinding.ItemArticleBinding
import com.compose.newsapp.domain.model.Article
import com.compose.newsapp.presentation.bookmarks.BookmarksFragment




interface OnArticleInteractionListener {
    fun onArticleClick(article: Article)
    fun onBookmarkToggle(article: Article) // Add this
}

class ArticlePagingAdapter(
    private val listener: OnArticleInteractionListener // Use new listener

) : PagingDataAdapter<Article, ArticlePagingAdapter.ArticleViewHolder>(ArticleDiffCallback) {


    override fun onBindViewHolder(holder: ArticlePagingAdapter.ArticleViewHolder, position: Int) {
        val article = getItem(position) // getItem is from PagingDataAdapter
        article?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ArticlePagingAdapter.ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding, listener)
    }

    class ArticleViewHolder(
        private val binding: ItemArticleBinding, // ViewBinding instance
        private val listener: OnArticleInteractionListener  // Listener instance
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            // Set click listener on the root view of the item
            binding.root.setOnClickListener {
                // Get article at this position - adapterPosition is reliable here
                // Need a way to get the article - Requires passing PagingAdapter instance or using getItem inside listener
                // Simpler: Pass the bound article directly in bind()
                // Handled in bind() method now
            }
        }

        fun bind(article: Article) {
            binding.textViewTitle.text = article.title
            binding.textViewSource.text = article.source.name
            binding.textViewPublishedAt.text = DateTimeUtils.formatIsoDateTime(article.publishedAt)
            binding.textViewDescription.text = article.description ?: "" // Handle null description

            // Image Loading (using Glide example)
            Glide.with(binding.imageViewArticle.context)
                .load(article.urlToImage)
                .placeholder(R.drawable.ic_placeholder_image) // Add a placeholder drawable
                .error(R.drawable.ic_placeholder_image) // Add an error drawable
                .centerCrop()
                .into(binding.imageViewArticle)

            // Update bookmark button state
            val bookmarkIconRes = if (article.isBookmarked) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border
            binding.buttonBookmarkToggle.setImageResource(bookmarkIconRes)

            // Set click listener for the whole item
            binding.root.setOnClickListener {
                listener.onArticleClick(article)
            }
            // Set click listener specifically for the bookmark toggle
            binding.buttonBookmarkToggle.setOnClickListener {
                listener.onBookmarkToggle(article)
            }
        }
    }

    // DiffUtil Callback object
    companion object ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            // ID is the unique identifier (using URL in our case)
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            // Check if content visible to the user has changed
            return oldItem == newItem // Data class checks all properties
        }
    }
}




