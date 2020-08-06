package com.andruid.magic.newsdaily.ui.adapter

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.andruid.magic.newsdaily.database.entity.NewsItem
import com.andruid.magic.newsdaily.ui.viewholder.NewsItemViewHolder

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<NewsItem>() {
    override fun areItemsTheSame(oldItem: NewsItem, newItem: NewsItem) =
        oldItem.title == newItem.title && oldItem.category == newItem.category

    override fun areContentsTheSame(oldItem: NewsItem, newItem: NewsItem) =
        oldItem == newItem
}

class NewsAdapter(private val newsClickListener: NewsClickListener) :
    PagingDataAdapter<NewsItem, NewsItemViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        NewsItemViewHolder.from(parent)

    override fun onBindViewHolder(holder: NewsItemViewHolder, position: Int) {
        getItem(position)?.let { news ->
            holder.bind(news, newsClickListener)
        }
    }

    interface NewsClickListener {
        fun onShareNews(news: NewsItem)
        fun onOpenNews(url: String)
    }
}