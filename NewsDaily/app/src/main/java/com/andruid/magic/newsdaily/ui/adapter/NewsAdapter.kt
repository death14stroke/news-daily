package com.andruid.magic.newsdaily.ui.adapter

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.andruid.magic.newsdaily.database.entity.NewsItem
import com.andruid.magic.newsdaily.ui.viewholder.NewsItemViewHolder
import com.andruid.magic.newsdaily.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.newsdaily.ui.viewmodel.NewsItemViewModel

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<NewsItem>() {
    override fun areItemsTheSame(oldItem: NewsItem, newItem: NewsItem) =
        oldItem.title == newItem.title && oldItem.category == newItem.category

    override fun areContentsTheSame(oldItem: NewsItem, newItem: NewsItem) =
        oldItem == newItem
}

class NewsAdapter(private val activity: AppCompatActivity) :
    PagingDataAdapter<NewsItem, NewsItemViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        NewsItemViewHolder.from(parent)

    override fun onBindViewHolder(holder: NewsItemViewHolder, position: Int) {
        getItem(position)?.let { news ->
            val viewModel =
                ViewModelProvider(activity, BaseViewModelFactory { NewsItemViewModel(news) }).get(
                    news.title,
                    NewsItemViewModel::class.java
                )
            holder.bind(viewModel)
        }
    }
}