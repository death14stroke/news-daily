package com.andruid.magic.newsdaily.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andruid.magic.newsdaily.databinding.LayoutCountryBinding;
import com.andruid.magic.newsdaily.viewholder.CountryViewHolder;
import com.blongho.country_data.Country;

import java.util.List;

public class CountryAdapter extends RecyclerView.Adapter<CountryViewHolder> {
    final private List<Country> countries;

    public CountryAdapter(List<Country> countries){
        this.countries = countries;
    }

    @NonNull
    @Override
    public CountryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LayoutCountryBinding binding = LayoutCountryBinding.inflate(inflater, parent, false);
        return new CountryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CountryViewHolder holder, int position) {
        Country country = countries.get(position);
        holder.bindCountry(country);
    }

    @Override
    public int getItemCount() {
        return countries.size();
    }
}