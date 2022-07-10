package com.death14stroke.newsdaily.ui.adapter

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.death14stroke.newsdaily.data.model.OpenNewsListener
import com.death14stroke.newsdaily.data.model.ShareNewsListener
import com.death14stroke.newsdaily.data.model.ViewImageListener
import com.death14stroke.newsdaily.ui.viewholder.NewsViewHolder
import com.death14stroke.newsloader.data.model.News

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<News>() {
    override fun areContentsTheSame(oldItem: News, newItem: News) =
        oldItem.url == newItem.url

    override fun areItemsTheSame(oldItem: News, newItem: News) =
        oldItem == newItem
}

class NewsAdapter(private val viewImageListener: ViewImageListener, private val openNewsListener: OpenNewsListener, private val shareNewsListener: ShareNewsListener) :
    PagingDataAdapter<News, NewsViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        NewsViewHolder.from(parent)

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        getItem(position)?.let { news ->
            holder.bind(news, viewImageListener, openNewsListener, shareNewsListener)
        }
    }
}