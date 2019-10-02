package com.andruid.magic.newsdaily.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.adapter.NewsAdapter;
import com.andruid.magic.newsdaily.databinding.ActivityMainBinding;
import com.andruid.magic.newsdaily.headlines.NewsViewModel;
import com.andruid.magic.newsdaily.headlines.NewsViewModelFactory;
import com.andruid.magic.newsdaily.util.AssetsUtil;
import com.cleveroad.loopbar.widget.OnItemClickListener;

import java.io.IOException;
import java.util.List;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {
    private ActivityMainBinding binding;
    private List<String> categories;
    private String category;
    private NewsViewModel newsViewModel;
    private NewsAdapter newsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        newsAdapter = new NewsAdapter();
        loadCategories();
        binding.loopBar.addOnItemClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.unbind();
    }

    @Override
    public void onItemClicked(int position) {
        category = categories.get(position);
    }

    private void loadCategories() {
        try {
            categories = AssetsUtil.readCategories(getAssets());
            Timber.d("categories try %d", categories.size());
        } catch (IOException e) {
            Timber.d("categories catch");
            e.printStackTrace();
        }
    }
}