package com.andruid.magic.newsdaily.ui.fragment

import android.os.Bundle
import android.view.Menu
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.pref.CountryPreference
import com.andruid.magic.newsdaily.pref.CountryPreferenceDialogFragment.Companion.newInstance
import com.andruid.magic.newsdaily.util.PrefUtil.getDefaultCountry

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_search).isVisible = false
        menu.findItem(R.id.action_settings).isVisible = false
    }

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
            dialogFragment = newInstance(preference.key)
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(requireFragmentManager(), "countryPref")
        } else
            super.onDisplayPreferenceDialog(preference)
    }
}