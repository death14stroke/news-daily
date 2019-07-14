package com.andruid.magic.newsdaily.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

import com.andruid.magic.newsdaily.databinding.LayoutNewsBinding;
import com.andruid.magic.newsdaily.viewholder.NewsViewHolder;
import com.andruid.magic.newsloader.model.News;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends PagedListAdapter<News, NewsViewHolder> {
    private static final DiffUtil.ItemCallback<News> DIFF_CALLBACK = new DiffUtil.ItemCallback<News>() {
        @Override
        public boolean areItemsTheSame(@NonNull News oldItem, @NonNull News newItem) {
            return oldItem.getTitle().compareTo(newItem.getTitle())==0;
        }

        @Override
        public boolean areContentsTheSame(@NonNull News oldItem, @NonNull News newItem) {
            return oldItem.equals(newItem);
        }
    };

    public NewsAdapter(){
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LayoutNewsBinding binding = LayoutNewsBinding.inflate(inflater, parent, false);
        return new NewsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = getItem(position);
        holder.bind(news);
    }

    public List<News> getNewsList(){
        PagedList<News> newsPagedList = getCurrentList();
        if(newsPagedList != null)
            return new ArrayList<>(newsPagedList.snapshot());
        return new ArrayList<>();
    }
}