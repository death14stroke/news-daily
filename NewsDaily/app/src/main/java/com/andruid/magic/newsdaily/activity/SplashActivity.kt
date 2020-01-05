package com.andruid.magic.newsdaily.activity

import android.content.Intent
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.util.PrefUtil.Companion.isFirstTime
import com.andruid.magic.newsdaily.util.PrefUtil.Companion.updateFirstTimePref
import com.daimajia.androidanimations.library.Techniques
import com.viksaa.sssplash.lib.activity.AwesomeSplash
import com.viksaa.sssplash.lib.cnst.Flags
import com.viksaa.sssplash.lib.model.ConfigSplash

class SplashActivity : AwesomeSplash() {

    override fun initSplash(configSplash: ConfigSplash) {
        configSplash.apply {
            backgroundColor = R.color.colorSplash
            animCircularRevealDuration = 1500
            revealFlagX = Flags.REVEAL_RIGHT
            revealFlagY = Flags.REVEAL_BOTTOM

            logoSplash = R.mipmap.ic_launcher_foreground
            animLogoSplashDuration = 1500
            animLogoSplashTechnique = Techniques.Bounce

            titleSplash = getString(R.string.app_name)
            titleTextColor = R.color.colorPrimary
            titleTextSize = 30f
            animTitleDuration = 1500
            animTitleTechnique = Techniques.FlipInX
        }
    }

    override fun animationsFinished() {
        if (isFirstTime(this@SplashActivity)) {
            startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
            finish()
            updateFirstTimePref(this)
        } else {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
}