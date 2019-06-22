package com.andruid.magic.newsdaily.pref;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.andruid.magic.newsdaily.adapter.CountryAdapter;
import com.andruid.magic.newsdaily.databinding.PrefDialogCountryBinding;
import com.andruid.magic.newsdaily.eventbus.CountryEvent;
import com.andruid.magic.newsdaily.util.AssetsUtil;
import com.blongho.country_data.Country;
import com.blongho.country_data.World;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class CountryPreferenceDialogFragment extends PreferenceDialogFragmentCompat {
    private CountryAdapter countryAdapter;
    private String country;

    public static CountryPreferenceDialogFragment newInstance(String key){
        CountryPreferenceDialogFragment fragment = new CountryPreferenceDialogFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.tag("preflog").d("register bus on resume");
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        Timber.tag("preflog").d("unregister bus on pause");
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if(positiveResult && country != null){
            Toast.makeText(getContext(), World.getCountryFrom(country).getName(), Toast.LENGTH_SHORT).show();
            DialogPreference preference = getPreference();
            if(preference instanceof CountryPreference){
                ((CountryPreference)preference).setCountry(country);
            }
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        PrefDialogCountryBinding binding = PrefDialogCountryBinding.bind(view);
        if(binding == null) {
            Timber.tag("preflog").d("null binding in pref dialog frag");
            return;
        }
        else
            Timber.tag("preflog").d("found binding in pref dialog frag");
        try {
            List<Country> countries = AssetsUtil.getCountries(Objects.requireNonNull(getContext())
                    .getAssets());
            countryAdapter = new CountryAdapter(countries);
            binding.recyclerView.setAdapter(countryAdapter);
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
            binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                    DividerItemDecoration.VERTICAL));
            Timber.tag("preflog").d("adapter set");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onCountryEvent(CountryEvent countryEvent){
        country = countryEvent.getCountryCode();
        Timber.tag("preflog").d("country clicked: %s", country);
        Objects.requireNonNull(getDialog()).dismiss();
    }
}