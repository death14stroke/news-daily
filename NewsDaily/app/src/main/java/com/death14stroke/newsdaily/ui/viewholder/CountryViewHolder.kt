package com.death14stroke.newsdaily.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.blongho.country_data.Country
import com.death14stroke.newsdaily.R
import com.death14stroke.newsdaily.data.model.CountryClickListener
import com.death14stroke.newsdaily.databinding.LayoutCountryBinding
import com.death14stroke.newsdaily.ui.fragment.SettingsFragment

/**
 * ViewHolder for the country items in [SettingsFragment]
 */
class CountryViewHolder(private val binding: LayoutCountryBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): CountryViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutCountryBinding>(
                inflater, R.layout.layout_country,
                parent, false
            )
            return CountryViewHolder(binding)
        }
    }

    fun bind(countryData: Country, countryClickListener: CountryClickListener) {
        binding.apply {
            country = countryData
            root.setOnClickListener { countryClickListener.invoke(countryData) }
            executePendingBindings()
        }
    }
}