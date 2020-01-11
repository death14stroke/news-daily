package com.andruid.magic.newsdaily.activity

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.andruid.magic.newsdaily.R
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import com.github.paolorotolo.appintro.model.SliderPagerBuilder

class IntroActivity : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPage(getString(R.string.categories), getString(R.string.categories_desc), R.mipmap.ic_launcher,
                R.color.colorSplash, R.color.colorPrimaryDark, R.color.colorPrimary)
        addPage(getString(R.string.audio_news), getString(R.string.audio_news_desc),
                R.drawable.ic_speak, R.color.colorBg2, R.color.colorDesc2, R.color.colorTitle2)
        showSkipButton(true)
        isProgressButtonEnabled = true
        setVibrate(true)
        setVibrateIntensity(30)
    }

    private fun addPage(title: String, desc: String, image: Int, bgColor: Int, descColor: Int,
                        titleColor: Int) {
        val sliderPage = SliderPagerBuilder()
                .title(title)
                .description(desc)
                .imageDrawable(image)
                .bgColor(ContextCompat.getColor(this, bgColor))
                .descColor(ContextCompat.getColor(this, descColor))
                .titleColor(ContextCompat.getColor(this, titleColor))
                .build()
        addSlide(AppIntroFragment.newInstance(sliderPage))
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        goToHomeScreen()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        goToHomeScreen()
    }

    private fun goToHomeScreen() {
        val intent = Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
        finish()
    }
}