package com.andruid.magic.newsdaily.ui.util

import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import java.text.DateFormat

object DataBindingAdapter {
    @JvmStatic
    @BindingAdapter("dateFromMs")
    fun formatDate(textView: TextView, ms: Long) {
        val dateFormat = DateFormat.getDateInstance()
        val date = dateFormat.format(ms)
        textView.text = date
    }

    @JvmStatic
    @BindingAdapter("readMore")
    fun readMoreUrl(textView: TextView, url: String) {
        val str = "<u>$url</u>"
        textView.text = HtmlCompat.fromHtml(str, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
}