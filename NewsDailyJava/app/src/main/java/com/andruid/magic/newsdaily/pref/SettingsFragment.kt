package com.andruid.magic.newsdaily.pref

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.pref.CountryPreferenceDialogFragment.Companion.newInstance
import com.andruid.magic.newsdaily.util.PrefUtil.Companion.getDefaultCountry

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.app_preferences)
        val countryPreference: CountryPreference? = findPreference(getString(R.string.pref_country))
        val defCountry = getDefaultCountry(context)
        val country = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(getString(R.string.pref_country), defCountry)
        countryPreference?.country = country ?: "in"
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        val dialogFragment: DialogFragment?
        if (preference is CountryPreference) {
            dialogFragment = newInstance(preference.getKey())
            dialogFragment?.setTargetFragment(this, 0)
            dialogFragment?.show(parentFragmentManager, "countryPref")
        }
        else
            super.onDisplayPreferenceDialog(preference)
    }
}