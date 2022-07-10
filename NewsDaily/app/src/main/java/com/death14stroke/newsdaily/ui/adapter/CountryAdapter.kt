package com.death14stroke.newsdaily.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.blongho.country_data.Country
import com.death14stroke.newsdaily.data.model.CountryClickListener
import com.death14stroke.newsdaily.ui.viewholder.CountryViewHolder

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Country>() {
    override fun areItemsTheSame(oldItem: Country, newItem: Country) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Country, newItem: Country) =
        oldItem.id == newItem.id && oldItem.name == newItem.name &&
                oldItem.flagResource == newItem.flagResource
}

class CountryAdapter(private val countryClickListener: CountryClickListener) :
    ListAdapter<Country, CountryViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder =
        CountryViewHolder.from(parent)

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val country = getItem(position)
        holder.bind(country, countryClickListener)
    }
}