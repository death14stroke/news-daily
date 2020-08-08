package com.andruid.magic.newsdaily.ui.fragment

import android.view.View
import androidx.core.os.bundleOf
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import com.andruid.magic.newsdaily.databinding.PrefDialogCountryBinding
import com.andruid.magic.newsdaily.ui.adapter.CountryAdapter
import com.andruid.magic.newsdaily.ui.custom.CountryPreference
import com.andruid.magic.newsdaily.util.getCountries
import com.andruid.magic.newsdaily.util.getSelectedCountry
import com.blongho.country_data.Country
import java.io.IOException
import java.util.*

class CountryPreferenceDialogFragment : PreferenceDialogFragmentCompat(),
    CountryAdapter.CountryClickListener {
    private val countryAdapter by lazy { CountryAdapter(this) }

    private lateinit var countryCode: String
    private lateinit var binding: PrefDialogCountryBinding

    companion object {
        fun newInstance(key: String): CountryPreferenceDialogFragment {
            return CountryPreferenceDialogFragment().apply {
                arguments = bundleOf(ARG_KEY to key)
            }
        }
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        countryCode = requireContext().getSelectedCountry()
        binding = PrefDialogCountryBinding.bind(view)

        try {
            val countries = requireContext().getCountries()
            countryAdapter.submitList(countries)

            binding.recyclerView.apply {
                adapter = countryAdapter
                itemAnimator = DefaultItemAnimator()
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val preference = preference
            if (preference is CountryPreference)
                preference.country = countryCode
        }
    }

    override fun onCountryClick(country: Country) {
        countryCode = country.alpha2.toLowerCase(Locale.ENGLISH)
        dialog?.dismiss()
        onDialogClosed(true)
    }
}