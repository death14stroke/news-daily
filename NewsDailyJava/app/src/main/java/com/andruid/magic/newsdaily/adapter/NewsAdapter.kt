package com.andruid.magic.newsdaily.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.andruid.magic.newsdaily.databinding.LayoutNewsBinding
import com.andruid.magic.newsdaily.viewholder.NewsViewHolder
import com.andruid.magic.newsloader.model.News
import java.util.*
import kotlin.collections.ArrayList


class NewsAdapter : PagedListAdapter<News, NewsViewHolder>(NewsDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutNewsBinding.inflate(inflater, parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = getItem(position)
        news?.apply { holder.bind(this) }
    }

    fun getNewsList(): List<News>? {
        val newsPagedList = currentList
        return newsPagedList?.apply {
            ArrayList(snapshot())
        } ?: emptyList()
    }

    class NewsDiffCallback : DiffUtil.ItemCallback<News>() {
        override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem == newItem
        }
    }
}