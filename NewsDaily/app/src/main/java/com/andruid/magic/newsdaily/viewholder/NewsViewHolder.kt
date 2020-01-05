package com.andruid.magic.newsdaily.viewholder

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.data.Constants
import com.andruid.magic.newsdaily.databinding.LayoutNewsBinding
import com.andruid.magic.newsdaily.eventbus.NewsEvent
import com.andruid.magic.newsloader.model.News
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import org.greenrobot.eventbus.EventBus

class NewsViewHolder(val binding : LayoutNewsBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(news: News) {
        val target: Target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                binding.imageView.setImageBitmap(bitmap)
                Palette.from(bitmap)
                        .generate { palette: Palette? ->
                            if (palette == null)
                                return@generate
                            val startCol = palette.getDarkVibrantColor(Color.BLUE)
                            val centerCol = palette.getDarkMutedColor(Color.BLACK)
                            val colors = intArrayOf(startCol, centerCol, startCol)
                            val drawable = GradientDrawable()
                            drawable.colors = colors
                            drawable.orientation = GradientDrawable.Orientation.TL_BR
                            binding.goToUrlTV.background = drawable
                        }
            }

            override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {
                e.printStackTrace()
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable) {
                binding.imageView.setImageDrawable(placeHolderDrawable)
            }
        }

        Picasso.get()
                .load(news.imageUrl)
                .resize(300, 300)
                .placeholder(R.drawable.ic_launcher_background)
                .into(target)

        binding.apply {
            this.news = news
            imageView.tag = target
            executePendingBindings()

            shareBtn.setOnClickListener {
                EventBus.getDefault().post(NewsEvent(news, Constants.ACTION_SHARE_NEWS))
            }
            goToUrlTV.setOnClickListener {
                EventBus.getDefault().post(NewsEvent(news, Constants.ACTION_OPEN_URL))
            }
        }
    }
}