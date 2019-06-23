package com.andruid.magic.newsdaily.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.andruid.magic.newsdaily.R;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;
import com.github.paolorotolo.appintro.model.SliderPagerBuilder;

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPage("Categories", "Get news from 7 different categories - General, Business, " +
                "Entertainment, Health, Science, Sports and Technology", R.mipmap.ic_launcher,
                android.R.color.holo_orange_light);
        addPage("Audio news", "Want to keep updated but no time to read? Listen to latest" +
                " news while working", R.drawable.ic_speak, android.R.color.holo_green_light);
        setBarColor(Color.parseColor("#3F51B5"));
        setSeparatorColor(Color.parseColor("#2196F3"));
        showSkipButton(true);
        setProgressButtonEnabled(true);
        setVibrate(true);
        setVibrateIntensity(30);
    }

    private void addPage(String title, String desc, int image, int bgColor){
        SliderPage sliderPage = new SliderPagerBuilder()
                .title(title)
                .description(desc)
                .imageDrawable(image)
                .bgColor(ContextCompat.getColor(this, bgColor))
                .build();
        addSlide(AppIntroFragment.newInstance(sliderPage));
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        startActivity(new Intent(this, HomeActivity.class));
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        startActivity(new Intent(this, HomeActivity.class));
    }
}