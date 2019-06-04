package com.andruid.magic.newsdaily.viewholder;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.andruid.magic.newsdaily.databinding.LayoutNewsBinding;
import com.andruid.magic.newsloader.model.News;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class NewsViewHolder extends RecyclerView.ViewHolder{
    private LayoutNewsBinding binding;
    private CardControlsListener mListener;

    public NewsViewHolder(LayoutNewsBinding binding, CardControlsListener mListener) {
        super(binding.getRoot());
        this.binding = binding;
        this.mListener = mListener;
    }

    public void bind(News news){
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                binding.imageView.setImageBitmap(bitmap);
                Palette.from(bitmap)
                        .generate(palette -> {
                            if(palette == null)
                                return;
                            int startCol = palette.getDarkVibrantColor(Color.BLUE);
                            int centerCol = palette.getDarkMutedColor(Color.BLACK);
                            int[] colors = {startCol, centerCol, startCol};
                            GradientDrawable drawable = new GradientDrawable();
                            drawable.setColors(colors);
                            drawable.setOrientation(GradientDrawable.Orientation.TL_BR);
                            binding.goToUrlTV.setBackground(drawable);
                        });
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                e.printStackTrace();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        };
        binding.imageView.setTag(target);
        Picasso.get()
                .load(news.getImageUrl())
                .into(target);
        binding.shareBtn.setOnClickListener(v -> mListener.onShareNews(news));
        binding.goToUrlTV.setOnClickListener(v -> mListener.onLoadUrl(news.getUrl()));
        binding.setNews(news);
        binding.executePendingBindings();
    }

    public interface CardControlsListener{
        void onLoadUrl(String url);
        void onShareNews(News news);
    }
}