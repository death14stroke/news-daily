package com.andruid.magic.newsdaily.pref

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.andruid.magic.newsdaily.adapter.CountryAdapter
import com.andruid.magic.newsdaily.databinding.PrefDialogCountryBinding
import com.andruid.magic.newsdaily.eventbus.CountryEvent
import com.andruid.magic.newsdaily.util.AssetsUtil.Companion.getCountries
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import java.io.IOException

class CountryPreferenceDialogFragment : PreferenceDialogFragmentCompat() {
    var country : String = ""

    companion object {
        @JvmStatic
        fun newInstance(key: String?): CountryPreferenceDialogFragment? {
            val fragment = CountryPreferenceDialogFragment()
            val b = Bundle(1).apply { putString(ARG_KEY, key) }
            fragment.arguments = b
            return fragment
        }
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        val binding = PrefDialogCountryBinding.bind(view)
        try {
            val countries = getCountries(context?.assets)
            val countryAdapter = CountryAdapter(countries)
            binding.recyclerView.apply {
                adapter = countryAdapter
                layoutManager = LinearLayoutManager(context)
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
                preference.country = country
        }
    }

    @Subscribe
    fun onCountryEvent(countryEvent: CountryEvent) {
        country = countryEvent.countryCode
        Timber.d("country clicked: $country")
        dialog?.dismiss()
        onDialogClosed(true)
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }
}