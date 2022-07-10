package com.death14stroke.newsdaily.ui.custom

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.death14stroke.newsdaily.R
import com.death14stroke.newsdaily.data.repository.MainRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CountryPreference(
    context: Context, attrs: AttributeSet
) : DialogPreference(context, attrs), KoinComponent {
    private val repository by inject<MainRepository>()
    var country: String = ""
        set(value) {
            persistString(value)
            summary = repository.getCountryFromCode(value).name
        }

    override fun getDialogLayoutResource() = R.layout.pref_dialog_country
}