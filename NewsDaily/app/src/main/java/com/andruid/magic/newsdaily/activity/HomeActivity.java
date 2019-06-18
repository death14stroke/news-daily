package com.andruid.magic.newsdaily.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.adapter.CustomPagerAdapter;
import com.andruid.magic.newsdaily.databinding.ActivityHomeBinding;
import com.andruid.magic.newsdaily.util.AssetsUtil;

import java.io.IOException;
import java.util.List;

import timber.log.Timber;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "assetslog";
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        setSupportActionBar(binding.toolBar);
        setUpViewPager();
    }

    private void setUpViewPager() {
        try {
            List<String> categories = AssetsUtil.readCategories(getAssets());
            Timber.tag(TAG).d("categories try %d", categories.size());
            CustomPagerAdapter pagerAdapter = new CustomPagerAdapter(this, getSupportFragmentManager(),
                    categories);
            binding.viewPager.setOffscreenPageLimit(0);
            binding.viewPager.setAdapter(pagerAdapter);
            binding.loopBar.setupWithViewPager(binding.viewPager);
        } catch (IOException e) {
            Timber.tag(TAG).d("categories catch");
            e.printStackTrace();
        }
    }
}