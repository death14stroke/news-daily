package com.andruid.magic.newsdaily.util

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.database.entity.NewsItem
import com.andruid.magic.newsdaily.ui.custom.CustomTabHelper

fun Activity.shareNews(news: NewsItem) {
    ShareCompat.IntentBuilder.from(this)
        .setSubject(news.title)
        .setText(news.url)
        .setType("text/plain")
        .setChooserTitle("Share this news with...")
        .startChooser()
}

fun Context.openChromeCustomTab(url: String, onFailed: () -> Unit) {
    val builder = CustomTabsIntent.Builder()
        .setToolbarColor(getColorFromAttr(R.attr.colorPrimary))
        .setSecondaryToolbarColor(getColorFromAttr(R.attr.colorSecondaryVariant))
        .addDefaultShareMenuItem()
        .setShowTitle(true)
        .setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left)
        .setExitAnimations(this, R.anim.slide_in_left, R.anim.slide_out_right)

    val packageName = CustomTabHelper.getPackageNameToUse(this, url)
    if (packageName != null) {
        val customTabsIntent = builder.build().also { it.intent.setPackage(packageName) }
        customTabsIntent.launchUrl(this, Uri.parse(url))
    } else {
        onFailed()
    }
}