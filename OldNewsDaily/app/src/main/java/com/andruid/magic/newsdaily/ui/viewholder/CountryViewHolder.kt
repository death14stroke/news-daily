package com.andruid.magic.newsdaily.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.databinding.LayoutCountryBinding
import com.andruid.magic.newsdaily.eventbus.CountryEvent
import com.blongho.country_data.Country
import org.greenrobot.eventbus.EventBus

class CountryViewHolder(val binding : LayoutCountryBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        @JvmStatic
        fun from(parent: ViewGroup): CountryViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<LayoutCountryBinding>(inflater, R.layout.layout_country,
                parent, false)
            return CountryViewHolder(binding)
        }
    }

    fun bind(country: Country) {
        binding.apply {
            this.country = country
            root.setOnClickListener { EventBus.getDefault().post(CountryEvent(country.alpha2)) }
            executePendingBindings()
        }
    }
}