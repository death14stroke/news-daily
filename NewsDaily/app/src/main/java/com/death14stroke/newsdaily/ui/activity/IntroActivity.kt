package com.death14stroke.newsdaily.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.death14stroke.newsdaily.R
import com.death14stroke.newsdaily.data.repository.MainRepository
import com.death14stroke.newsdaily.ui.fragment.IntroFragment
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroPageTransformerType
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class IntroActivity: AppIntro2() {
    private val repository: MainRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(
            IntroFragment.newInstance(
                title = R.string.countries_title,
                desc = R.string.countries_desc,
                backgroundColor = R.drawable.gradient_left,
                lottieRes = R.raw.countries
            )
        )
        addSlide(
            IntroFragment.newInstance(
                title = R.string.categories_title,
                desc = R.string.categories_desc,
                backgroundColor = R.drawable.gradient_center,
                lottieRes = R.raw.categories
            )
        )
        addSlide(
            IntroFragment.newInstance(
                title = R.string.audio_news_title,
                desc = R.string.audio_news_desc,
                backgroundColor = R.drawable.gradient_right,
                lottieRes = R.raw.audio
            )
        )

        isSkipButtonEnabled = true
        isVibrate = true
        vibrateDuration = 30
        setTransformer(AppIntroPageTransformerType.Flow)
    }

    override fun onResume() {
        super.onResume()
        setImmersiveMode()
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        lifecycleScope.launch { goToHomeScreen() }
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        lifecycleScope.launch { goToHomeScreen() }
    }

    private suspend fun goToHomeScreen() {
        repository.markFirstTimeDone()
        val intent = Intent(this, HomeActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
        finish()
    }
}