package com.andruid.magic.newsdaily.ui.viewholder

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.database.entity.NewsItem
import com.andruid.magic.newsdaily.databinding.LayoutNewsBinding
import com.andruid.magic.newsdaily.ui.viewmodel.NewsItemViewModel
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class NewsItemViewHolder(private val binding: LayoutNewsBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): NewsItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutNewsBinding>(
                inflater, R.layout.layout_news, parent, false
            )

            return NewsItemViewHolder(binding)
        }
    }

    fun bind(viewModel: NewsItemViewModel) {
        loadImage(binding, viewModel.news)

        binding.viewModel = viewModel
        binding.executePendingBindings()
    }

    private fun loadImage(binding: LayoutNewsBinding, news: NewsItem) {
        val target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                binding.imageView.setImageBitmap(bitmap)
                Palette.from(bitmap)
                    .generate { palette: Palette? ->
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

            override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                binding.imageView.setImageDrawable(errorDrawable)
                e.printStackTrace()
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable) =
                binding.imageView.setImageDrawable(placeHolderDrawable)
        }

        binding.imageView.tag = target

        Picasso.get()
            .load(news.imageUrl)
            .resize(300, 300)
            .placeholder(R.drawable.ic_launcher_background)
            .into(target)
    }
}