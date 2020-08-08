package com.andruid.magic.newsdaily.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.util.color
import com.andruid.magic.newsdaily.util.updateFirstTimePref
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.github.appintro.model.SliderPagerBuilder

class IntroActivity : AppIntro2() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPage(
            getString(R.string.categories),
            getString(R.string.categories_desc),
            R.mipmap.ic_launcher,
            R.color.colorSplash,
            R.color.colorPrimaryDark,
            R.color.colorPrimary
        )
        addPage(
            getString(R.string.audio_news),
            getString(R.string.audio_news_desc),
            R.drawable.ic_speak,
            R.color.colorBg2,
            R.color.colorDesc2,
            R.color.colorTitle2
        )

        isSkipButtonEnabled = true
        isVibrate = true
        vibrateDuration = 30
        setTransformer(AppIntroPageTransformerType.Fade)
    }

    override fun onResume() {
        super.onResume()
        setImmersiveMode()
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        goToHomeScreen()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        updateFirstTimePref()
        goToHomeScreen()
    }

    private fun addPage(
        title: String,
        desc: String,
        @DrawableRes image: Int,
        @ColorRes bgColor: Int,
        @ColorRes descColor: Int,
        @ColorRes titleColor: Int
    ) {
        val sliderPage = SliderPagerBuilder()
            .title(title)
            .description(desc)
            .imageDrawable(image)
            .backgroundColor(color(bgColor))
            .descriptionColor(color(descColor))
            .titleColor(color(titleColor))
            .build()
        addSlide(AppIntroFragment.newInstance(sliderPage))
    }

    private fun goToHomeScreen() {
        val intent = Intent(this, HomeActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
        finish()
    }
}