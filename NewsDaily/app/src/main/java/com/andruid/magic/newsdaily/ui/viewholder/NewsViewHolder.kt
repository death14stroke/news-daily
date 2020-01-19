package com.andruid.magic.newsdaily.ui.viewholder

import android.annotation.SuppressLint
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
import com.andruid.magic.newsdaily.databinding.LayoutNewsBinding
import com.andruid.magic.newsdaily.ui.viewmodel.NewsItemViewModel
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class NewsViewHolder(private val binding: LayoutNewsBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        @JvmStatic
        fun from(parent: ViewGroup): NewsViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutNewsBinding>(
                inflater,
                R.layout.layout_news, parent, false
            )
            return NewsViewHolder(binding)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun bind(viewModel: NewsItemViewModel) {
        binding.viewModel = viewModel
        binding.executePendingBindings()

        binding.apply {
            val target = object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                    imageView.setImageBitmap(bitmap)
                    Palette.from(bitmap)
                        .generate { palette: Palette? ->
                            palette?.let {
                                val startCol = it.getDarkVibrantColor(Color.BLUE)
                                val centerCol = it.getDarkMutedColor(Color.BLACK)
                                val colors = intArrayOf(startCol, centerCol, startCol)
                                val drawable = GradientDrawable()
                                drawable.colors = colors
                                drawable.orientation = GradientDrawable.Orientation.TL_BR
                                goToUrlTV.background = drawable
                            }
                        }
                }

                override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) =
                    e.printStackTrace()

                override fun onPrepareLoad(placeHolderDrawable: Drawable) =
                    imageView.setImageDrawable(placeHolderDrawable)
            }
            imageView.tag = target

            Picasso.get()
                .load(viewModel.newsOnline.imageUrl)
                .resize(300, 300)
                .placeholder(R.drawable.ic_launcher_background)
                .into(target)
        }
    }
}