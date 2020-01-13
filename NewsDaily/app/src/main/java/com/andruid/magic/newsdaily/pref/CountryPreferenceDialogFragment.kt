package com.andruid.magic.newsdaily.pref

import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.andruid.magic.newsdaily.ui.adapter.CountryAdapter
import com.andruid.magic.newsdaily.databinding.PrefDialogCountryBinding
import com.andruid.magic.newsdaily.eventbus.CountryEvent
import com.andruid.magic.newsdaily.util.AssetsUtil.Companion.getCountries
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.IOException

class CountryPreferenceDialogFragment : PreferenceDialogFragmentCompat() {
    var country: String = ""

    companion object {
        private val TAG = CountryPreferenceDialogFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(key: String): CountryPreferenceDialogFragment {
            val fragment = CountryPreferenceDialogFragment()
            fragment.arguments = bundleOf(ARG_KEY to key)
            return fragment
        }
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        val binding = PrefDialogCountryBinding.bind(view)
        try {
            val countries = getCountries(context?.assets)
            val countryAdapter = CountryAdapter()
            countryAdapter.submitList(countries)
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
        Log.d(TAG, "country clicked: $country")
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