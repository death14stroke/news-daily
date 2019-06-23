package com.andruid.magic.newsdaily.activity;

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.andruid.magic.newsdaily.R;
import com.daimajia.androidanimations.library.Techniques;
import com.viksaa.sssplash.lib.activity.AwesomeSplash;
import com.viksaa.sssplash.lib.cnst.Flags;
import com.viksaa.sssplash.lib.model.ConfigSplash;

public class SplashActivity extends AwesomeSplash {
    @Override
    public void initSplash(ConfigSplash configSplash) {
        configSplash.setBackgroundColor(R.color.colorSplash);
        configSplash.setAnimCircularRevealDuration(2000);
        configSplash.setRevealFlagX(Flags.REVEAL_RIGHT);
        configSplash.setRevealFlagY(Flags.REVEAL_BOTTOM);

        configSplash.setLogoSplash(R.mipmap.ic_launcher_foreground);
        configSplash.setAnimLogoSplashDuration(2000);
        configSplash.setAnimLogoSplashTechnique(Techniques.Bounce);

        configSplash.setTitleSplash(getString(R.string.app_name));
        configSplash.setTitleTextColor(R.color.colorPrimary);
        configSplash.setTitleTextSize(30f);
        configSplash.setAnimTitleDuration(3000);
        configSplash.setAnimTitleTechnique(Techniques.FlipInX);
    }

    @Override
    public void animationsFinished() {
        new Thread(() -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                    this);
            boolean isFirstStart = sharedPreferences.getBoolean(getString(R.string.first_start), true);
            if(isFirstStart){
                sharedPreferences.edit()
                        .putBoolean(getString(R.string.first_start), false)
                        .apply();
                runOnUiThread(() -> {
                    startActivity(new Intent(this, IntroActivity.class));
                    finish();
                });
            }
            else{
                startActivity(new Intent(this, HomeActivity.class));
            }
        }).start();
    }
}