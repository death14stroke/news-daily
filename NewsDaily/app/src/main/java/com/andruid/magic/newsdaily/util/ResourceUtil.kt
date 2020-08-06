package com.andruid.magic.newsdaily.util

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

fun Context.color(@ColorRes colorRes: Int) =
    ContextCompat.getColor(this, colorRes)

fun Fragment.color(@ColorRes colorRes: Int) =
    requireContext().color(colorRes)