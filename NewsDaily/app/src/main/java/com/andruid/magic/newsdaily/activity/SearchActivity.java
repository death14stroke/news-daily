package com.andruid.magic.newsdaily.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.databinding.ActivitySearchBinding;

public class SearchActivity extends AppCompatActivity {
    private ActivitySearchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.unbind();
    }
}