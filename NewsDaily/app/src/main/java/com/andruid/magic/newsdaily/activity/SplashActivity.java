package com.andruid.magic.newsdaily.activity;

import android.content.Intent;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.util.PrefUtil;
import com.daimajia.androidanimations.library.Techniques;
import com.viksaa.sssplash.lib.activity.AwesomeSplash;
import com.viksaa.sssplash.lib.cnst.Flags;
import com.viksaa.sssplash.lib.model.ConfigSplash;

import java.util.concurrent.Executors;

public class SplashActivity extends AwesomeSplash {
    @Override
    public void initSplash(ConfigSplash configSplash) {
        configSplash.setBackgroundColor(R.color.colorSplash);
        configSplash.setAnimCircularRevealDuration(1500);
        configSplash.setRevealFlagX(Flags.REVEAL_RIGHT);
        configSplash.setRevealFlagY(Flags.REVEAL_BOTTOM);

        configSplash.setLogoSplash(R.mipmap.ic_launcher_foreground);
        configSplash.setAnimLogoSplashDuration(1500);
        configSplash.setAnimLogoSplashTechnique(Techniques.Bounce);

        configSplash.setTitleSplash(getString(R.string.app_name));
        configSplash.setTitleTextColor(R.color.colorPrimary);
        configSplash.setTitleTextSize(30f);
        configSplash.setAnimTitleDuration(1500);
        configSplash.setAnimTitleTechnique(Techniques.FlipInX);
    }

    @Override
    public void animationsFinished() {
        Executors.newSingleThreadExecutor().execute(() -> {
            if(PrefUtil.isFirstTime(SplashActivity.this)){
                runOnUiThread(() -> {
                    startActivity(new Intent(SplashActivity.this, IntroActivity.class));
                    finish();
                });
                PrefUtil.updateFirstTimePref(this);
            }
            else{
                runOnUiThread(() -> {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                });
            }
        });
    }
}