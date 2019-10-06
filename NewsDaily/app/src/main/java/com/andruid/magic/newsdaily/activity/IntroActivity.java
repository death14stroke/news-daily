package com.andruid.magic.newsdaily.activity;

import android.content.Intent;
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
                R.color.colorSplash, R.color.colorPrimaryDark, R.color.colorPrimary);
        addPage("Audio news", "Want to keep updated but no time to read? Listen to latest" +
                " news while working", R.drawable.ic_speak, R.color.colorBg2, R.color.colorDesc2,
                R.color.colorTitle2);
        showSkipButton(true);
        setProgressButtonEnabled(true);
        setVibrate(true);
        setVibrateIntensity(30);
    }

    private void addPage(String title, String desc, int image, int bgColor, int descColor,
                         int titleColor){
        SliderPage sliderPage = new SliderPagerBuilder()
                .title(title)
                .description(desc)
                .imageDrawable(image)
                .bgColor(ContextCompat.getColor(this, bgColor))
                .descColor(ContextCompat.getColor(this, descColor))
                .titleColor(ContextCompat.getColor(this, titleColor))
                .build();
        addSlide(AppIntroFragment.newInstance(sliderPage));
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        goToHomeScreen();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        goToHomeScreen();
    }

    private void goToHomeScreen() {
        Intent intent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }
}