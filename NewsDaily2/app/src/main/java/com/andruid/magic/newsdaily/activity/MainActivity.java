package com.andruid.magic.newsdaily.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.databinding.ActivityMainBinding;
import com.andruid.magic.newsdaily.fragment.NewsFragment;
import com.andruid.magic.newsdaily.util.AssetsUtil;
import com.andruid.magic.newsdaily.util.StringUtil;
import com.cleveroad.loopbar.widget.OnItemClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {
    private static final String KEY_CATEGORIES = "categories";
    private ActivityMainBinding binding;
    private List<String> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.loopBar.addOnItemClickListener(this);
        if(savedInstanceState == null)
            loadCategories();
        else
            categories = savedInstanceState.getStringArrayList(KEY_CATEGORIES);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(KEY_CATEGORIES, (ArrayList<String>) categories);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.unbind();
    }

    private void loadCategories() {
        try {
            categories = AssetsUtil.readCategories(getAssets());
            loadFirstFrag();
            Timber.d("categories try %d", categories.size());
        } catch (IOException e) {
            Timber.e("categories not read exc");
            e.printStackTrace();
        }
    }

    private void loadFirstFrag() {
        String category = categories.get(0);
        Fragment fragment = NewsFragment.newInstance(category);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment, category)
                .commit();
        setTitle(StringUtil.capFirstLetter(category));
    }

    @Override
    public void onItemClicked(int position) {
        if(position >= 0 && position < categories.size()) {
            String category = categories.get(position);
            Fragment fragment = NewsFragment.newInstance(category);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment, category)
                    .commit();
            setTitle(StringUtil.capFirstLetter(category));
        }
    }
}