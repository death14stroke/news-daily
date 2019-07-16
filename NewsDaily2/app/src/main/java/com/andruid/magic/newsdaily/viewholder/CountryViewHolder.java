package com.andruid.magic.newsdaily.viewholder;

import androidx.recyclerview.widget.RecyclerView;

import com.andruid.magic.newsdaily.databinding.LayoutCountryBinding;
import com.andruid.magic.newsdaily.eventbus.CountryEvent;
import com.blongho.country_data.Country;

import org.greenrobot.eventbus.EventBus;

public class CountryViewHolder extends RecyclerView.ViewHolder {
    private LayoutCountryBinding binding;

    public CountryViewHolder(LayoutCountryBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bindCountry(Country country){
        binding.setCountry(country);
        binding.getRoot().setOnClickListener(v ->
                EventBus.getDefault().post(new CountryEvent(country.getAlpha2()))
        );
        binding.executePendingBindings();
    }
}