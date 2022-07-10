package com.death14stroke.newsdaily.ui.util

import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import coil.load
import java.text.DateFormat
import java.text.SimpleDateFormat

@BindingAdapter("dateFormat")
fun TextView.formatDate(dateStr: String) {
    val sdf = SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss'Z'")
    val date = sdf.parse(dateStr)!!
    val dateFormat = DateFormat.getDateInstance()
    text = dateFormat.format(date)
}

@BindingAdapter("readMore")
fun TextView.readMoreUrl(url: String?) {
    val str = "<u>$url</u>"
    text = HtmlCompat.fromHtml(str, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

@BindingAdapter("imageRes")
fun ImageView.loadImage(@DrawableRes res: Int) {
    load(res)
}
