package com.andruid.magic.newsdaily.pref

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.andruid.magic.newsdaily.R
import com.blongho.country_data.World

class CountryPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {
    var country: String = ""
        set(value) {
            persistString(value)
            summary = World.getCountryFrom(value).name
        }

    override fun getDialogLayoutResource() = R.layout.pref_dialog_country
}