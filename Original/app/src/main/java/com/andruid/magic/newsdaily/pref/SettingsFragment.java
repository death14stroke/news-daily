package com.andruid.magic.newsdaily.pref;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.util.PrefUtil;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {
    private CountryPreference countryPreference;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.app_preferences);
        countryPreference = findPreference(getString(R.string.pref_country));
        String defCountry = PrefUtil.getDefaultCountry(Objects.requireNonNull(getContext()));
        String country = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(
                getString(R.string.pref_country), defCountry);
        countryPreference.setCountry(country);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if(preference instanceof CountryPreference)
            dialogFragment = CountryPreferenceDialogFragment.newInstance(preference.getKey());
        if(dialogFragment != null){
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(Objects.requireNonNull(getFragmentManager()), "countryPref");
        }
        else
            super.onDisplayPreferenceDialog(preference);
    }
}