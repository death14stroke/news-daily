package com.andruid.magic.newsdaily.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.andruid.magic.newsdaily.databinding.LayoutCountryBinding
import com.andruid.magic.newsdaily.viewholder.CountryViewHolder
import com.blongho.country_data.Country

class CountryAdapter : ListAdapter<Country, CountryViewHolder>(CountryDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutCountryBinding.inflate(inflater, parent, false)
        return CountryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val country: Country = getItem(position)
        holder.bind(country)
    }

    class CountryDiffCallback : DiffUtil.ItemCallback<Country>() {
        override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem.id == newItem.id && oldItem.name == newItem.name &&
                    oldItem.flagResource == newItem.flagResource
        }

    }
}