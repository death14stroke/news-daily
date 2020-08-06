package com.andruid.magic.newsdaily.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.andruid.magic.newsdaily.ui.viewholder.CountryViewHolder
import com.blongho.country_data.Country

class CountryAdapter : ListAdapter<Country, CountryViewHolder>(CountryDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder =
        CountryViewHolder.from(parent)

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val country = getItem(position)
        holder.bind(country)
    }

    class CountryDiffCallback : DiffUtil.ItemCallback<Country>() {
        override fun areItemsTheSame(oldItem: Country, newItem: Country) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Country, newItem: Country) =
            oldItem.id == newItem.id && oldItem.name == newItem.name &&
                    oldItem.flagResource == newItem.flagResource
    }
}