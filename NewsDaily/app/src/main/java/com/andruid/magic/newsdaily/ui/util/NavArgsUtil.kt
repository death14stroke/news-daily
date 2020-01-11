package com.andruid.magic.newsdaily.ui.util

import android.os.Bundle
import androidx.core.os.bundleOf
import com.andruid.magic.newsdaily.data.Constants
import java.util.*

object NavArgsUtil {
    @JvmStatic
    fun buildNavArgs(category: String): Bundle {
        return bundleOf(
            Constants.EXTRA_CATEGORY to category.toLowerCase(Locale.getDefault())
        )
    }
}