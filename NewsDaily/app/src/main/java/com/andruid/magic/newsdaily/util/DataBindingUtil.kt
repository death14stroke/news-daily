package com.andruid.magic.newsdaily.util

import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import java.text.DateFormat

@BindingAdapter("dateFromMs")
fun TextView.formatDate(ms: Long) {
    val dateFormat = DateFormat.getDateInstance()
    val date = dateFormat.format(ms)

    text = date
}

@BindingAdapter("readMore")
fun TextView.readMoreUrl(url: String) {
    val str = "<u>$url</u>"
    text = HtmlCompat.fromHtml(str, HtmlCompat.FROM_HTML_MODE_LEGACY)
}