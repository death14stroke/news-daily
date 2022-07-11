package com.death14stroke.newsdaily.ui.viewholder

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.death14stroke.newsdaily.R
import com.death14stroke.newsdaily.data.model.OpenNewsListener
import com.death14stroke.newsdaily.data.model.ShareNewsListener
import com.death14stroke.newsdaily.data.model.ViewImageListener
import com.death14stroke.newsdaily.databinding.LayoutNewsBinding
import com.death14stroke.newsdaily.ui.fragment.NewsFragment
import com.death14stroke.newsdaily.ui.fragment.SearchFragment
import com.death14stroke.newsloader.data.model.News

/**
 * ViewHolder for the news items in [NewsFragment] and [SearchFragment]
 */
class NewsViewHolder(private val binding: LayoutNewsBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): NewsViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutNewsBinding>(
                inflater, R.layout.layout_news, parent, false
            )

            return NewsViewHolder(binding)
        }
    }

    fun bind(
        newsItem: News,
        viewImageListener: ViewImageListener,
        openNewsListener: OpenNewsListener,
        shareNewsListener: ShareNewsListener
    ) {
        loadImage(newsItem)

        binding.apply {
            news = newsItem
            imageView.transitionName = "iv_${newsItem.imageUrl}"
            imageView.setOnClickListener { viewImageListener.invoke(it, newsItem.imageUrl ?: "") }
            goToUrlTV.setOnClickListener { openNewsListener.invoke(newsItem.url) }
            shareBtn.setOnClickListener { shareNewsListener.invoke(newsItem) }
            executePendingBindings()
        }
    }

    private fun loadImage(news: News) {
        binding.imageView.load(news.imageUrl) {
            allowHardware(false)
            listener(
                onSuccess = { _, result ->
                    processBitmap(result.drawable.toBitmap())
                }
            )
        }
    }

    /**
     * Util to create color palette from news image for url textview background
     */
    private fun processBitmap(bitmap: Bitmap) {
        Palette.from(bitmap)
            .generate { palette ->
                palette?.let {
                    val startCol = it.getDarkVibrantColor(Color.BLUE)
                    val centerCol = it.getDarkMutedColor(Color.BLACK)
                    val gradientColors = intArrayOf(startCol, centerCol, startCol)

                    binding.goToUrlTV.background = GradientDrawable().apply {
                        colors = gradientColors
                        orientation = GradientDrawable.Orientation.TL_BR
                    }
                }
            }
    }
}