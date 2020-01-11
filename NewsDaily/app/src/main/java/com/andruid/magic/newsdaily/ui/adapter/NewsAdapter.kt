package com.andruid.magic.newsdaily.ui.adapter

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.andruid.magic.newsdaily.ui.viewholder.NewsViewHolder
import com.andruid.magic.newsloader.model.News

class NewsAdapter : PagedListAdapter<News, NewsViewHolder>(NewsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        return NewsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = getItem(position)
        news?.apply { holder.bind(this) }
    }

    fun getNewsList(): List<News> = currentList?.snapshot()?.toList() ?: emptyList()

    class NewsDiffCallback : DiffUtil.ItemCallback<News>() {
        override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem == newItem
        }
    }
}