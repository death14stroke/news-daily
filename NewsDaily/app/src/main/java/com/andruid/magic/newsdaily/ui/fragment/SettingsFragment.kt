package com.andruid.magic.newsdaily.ui.fragment

import android.os.Bundle
import android.view.Menu
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.ui.custom.CountryPreference
import com.andruid.magic.newsdaily.util.getSelectedCountry

class SettingsFragment : PreferenceFragmentCompat() {
    private val countryPreference by lazy {
        findPreference<CountryPreference>(getString(R.string.pref_country))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        menu.findItem(R.id.action_search).isVisible = false
        menu.findItem(R.id.action_settings).isVisible = false
        menu.findItem(R.id.action_intro).isVisible = false
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.app_preferences)

        val country = requireContext().getSelectedCountry()
        countryPreference?.country = country
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is CountryPreference) {
            CountryPreferenceDialogFragment.newInstance(preference.key).apply {
                setTargetFragment(this@SettingsFragment, 0)
            }.also {
                it.show(parentFragmentManager, "countryPref")
            }
        } else
            super.onDisplayPreferenceDialog(preference)
    }
}