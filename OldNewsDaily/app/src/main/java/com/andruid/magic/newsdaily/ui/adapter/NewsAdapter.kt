package com.andruid.magic.newsdaily.ui.adapter

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.andruid.magic.newsdaily.database.entity.News
import com.andruid.magic.newsdaily.ui.viewholder.NewsViewHolder
import com.andruid.magic.newsdaily.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.newsdaily.ui.viewmodel.NewsItemViewModel

class NewsAdapter(private val activity: AppCompatActivity) :
    PagedListAdapter<News, NewsViewHolder>(NewsDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NewsViewHolder.from(parent)

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = getItem(position)
        news?.let {
            val viewModel = ViewModelProvider(activity,
                BaseViewModelFactory { NewsItemViewModel(it) }).get(
                it.title,
                NewsItemViewModel::class.java
            )
            holder.bind(viewModel)
        }
    }

    fun getNewsList(): List<News> = currentList?.snapshot()?.toList() ?: emptyList()

    class NewsDiffCallback : DiffUtil.ItemCallback<News>() {
        override fun areItemsTheSame(oldItem: News, newItem: News) =
            oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: News, newItem: News) =
            oldItem == newItem
    }
}