package com.death14stroke.newsdaily.ui.custom

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs
import kotlin.math.max

class AlphaPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        page.apply {
            scaleX = max(0.9f, 1 - abs(position))
            scaleY = max(0.9f, 1 - abs(position))
        }

        when {
            position < -1 -> page.alpha = 0.1f
            position <= 1 -> page.alpha = max(0.2f, 1 - abs(position))
            else -> page.alpha = 0.1f
        }
    }
}