package com.andruid.magic.newsdaily.viewholder;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.databinding.LayoutNewsBinding;
import com.andruid.magic.newsdaily.eventbus.NewsEvent;
import com.andruid.magic.newsloader.model.News;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.greenrobot.eventbus.EventBus;

import static com.andruid.magic.newsdaily.data.Constants.ACTION_OPEN_URL;
import static com.andruid.magic.newsdaily.data.Constants.ACTION_SHARE_NEWS;

public class NewsViewHolder extends RecyclerView.ViewHolder{
    private LayoutNewsBinding binding;

    public NewsViewHolder(LayoutNewsBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
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
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                binding.imageView.setImageDrawable(placeHolderDrawable);
            }
        };
        binding.imageView.setTag(target);
        Picasso.get()
                .load(news.getImageUrl())
                .resize(300, 300)
                .placeholder(R.drawable.ic_launcher_background)
                .into(target);
        binding.shareBtn.setOnClickListener(v ->
                EventBus.getDefault().post(new NewsEvent(news, ACTION_SHARE_NEWS))
        );
        binding.goToUrlTV.setOnClickListener(v ->
                EventBus.getDefault().post(new NewsEvent(news, ACTION_OPEN_URL))
        );
        binding.setNews(news);
        binding.executePendingBindings();
    }
}