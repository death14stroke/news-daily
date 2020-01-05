package com.andruid.magic.newsdaily.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.newsdaily.databinding.LayoutCountryBinding
import com.andruid.magic.newsdaily.eventbus.CountryEvent
import com.blongho.country_data.Country
import org.greenrobot.eventbus.EventBus

class CountryViewHolder(val binding : LayoutCountryBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bindCountry(country: Country) {
        binding.apply {
            this.country = country
            root.setOnClickListener {
                EventBus.getDefault().post(CountryEvent(country.alpha2))
            }
            executePendingBindings()
        }
    }
}