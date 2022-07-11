package com.death14stroke.newsdaily.ui.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

@ColorInt
fun Context.color(@ColorRes colorRes: Int) =
    ContextCompat.getColor(this, colorRes)

@ColorInt
fun Fragment.color(@ColorRes colorRes: Int) =
    requireContext().color(colorRes)

/**
 * Util to get color [Int] from theme color attributes
 */
@ColorInt
fun Context.getColorFromAttr(@AttrRes attrColor: Int, resolveRefs: Boolean = true): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}