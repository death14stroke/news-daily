package com.death14stroke.newsdaily.ui.util

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.death14stroke.newsdaily.R
import com.death14stroke.newsdaily.data.model.OpenNewsListener
import com.death14stroke.newsdaily.data.model.ShareNewsListener
import com.death14stroke.newsdaily.data.model.ViewImageListener
import com.death14stroke.newsdaily.ui.custom.CustomTabHelper
import com.death14stroke.newsdaily.ui.fragment.NewsFragmentDirections
import com.death14stroke.newsloader.data.model.News


fun Activity.shareNews(news: News) {
    ShareCompat.IntentBuilder(this)
        .setSubject(news.title)
        .setText(news.url)
        .setType("text/plain")
        .setChooserTitle("Share this news with...")
        .startChooser()
}

fun Context.openChromeCustomTab(url: String, onFailed: () -> Unit) {
    val defaultColorSchemeParams = CustomTabColorSchemeParams.Builder()
        .setToolbarColor(getColorFromAttr(android.R.attr.colorPrimaryDark))
        .setSecondaryToolbarColor(getColorFromAttr(android.R.attr.colorSecondary))
        .build()
    val builder = CustomTabsIntent.Builder()
        .setDefaultColorSchemeParams(defaultColorSchemeParams)
        .setShareState(CustomTabsIntent.SHARE_STATE_ON)
        .setShowTitle(true)
        .setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left)
        .setExitAnimations(this, R.anim.slide_in_left, R.anim.slide_out_right)

    val packageName = CustomTabHelper.getPackageNameToUse(this, url)
    if (packageName != null) {
        val customTabsIntent = builder.build().also { it.intent.setPackage(packageName) }
        customTabsIntent.launchUrl(this, Uri.parse(url))
    } else
        onFailed()
}

fun Fragment.getViewImageListener(): ViewImageListener = { view, imageUrl ->
    val directions = NewsFragmentDirections.actionNewsToShowImage(imageUrl)
    val extras = FragmentNavigatorExtras(
        view to view.transitionName
    )
    findNavController().navigate(directions, extras)
}

fun Fragment.getOpenNewsListener(): OpenNewsListener = { url ->
    requireContext().openChromeCustomTab(url) {
        val directions = NewsFragmentDirections.actionNewsToWebview(url)
        findNavController().navigate(directions)
    }
}

fun Fragment.getShareNewsListener(): ShareNewsListener = { news ->
    requireActivity().shareNews(news)
}