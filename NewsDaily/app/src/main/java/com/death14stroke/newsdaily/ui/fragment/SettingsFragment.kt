package com.death14stroke.newsdaily.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.death14stroke.newsdaily.R
import com.death14stroke.newsdaily.data.repository.MainRepository
import com.death14stroke.newsdaily.ui.custom.CountryPreference
import com.death14stroke.newsdaily.ui.util.getColorFromAttr
import org.koin.android.ext.android.inject

class SettingsFragment : PreferenceFragmentCompat() {
    private val repository by inject<MainRepository>()
    private val countryPreference by lazy { findPreference<CountryPreference>(getString(R.string.pref_country)) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.app_preferences)
        val country = repository.getSelectedCountry()
        countryPreference?.country = country
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(requireContext().getColorFromAttr(com.google.android.material.R.attr.colorSurface))
    }

    @Suppress("DEPRECATION")
    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is CountryPreference) {
            val fragment = CountryPreferenceDialogFragment.newInstance(preference.key).apply {
                setTargetFragment(this@SettingsFragment, 0)
            }
            fragment.show(parentFragmentManager, "countryPref")
        } else
            super.onDisplayPreferenceDialog(preference)
    }
}