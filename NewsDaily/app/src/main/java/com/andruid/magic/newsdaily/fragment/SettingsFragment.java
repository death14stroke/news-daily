package com.andruid.magic.newsdaily.fragment;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.pref.CountryPreference;
import com.andruid.magic.newsdaily.pref.CountryPreferenceDialogFragment;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.app_preferences);
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