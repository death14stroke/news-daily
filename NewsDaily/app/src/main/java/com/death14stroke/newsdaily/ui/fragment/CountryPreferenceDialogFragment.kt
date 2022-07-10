package com.death14stroke.newsdaily.ui.fragment

import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import com.death14stroke.newsdaily.data.model.CountryClickListener
import com.death14stroke.newsdaily.databinding.PrefDialogCountryBinding
import com.death14stroke.newsdaily.ui.adapter.CountryAdapter
import com.death14stroke.newsdaily.ui.custom.CountryPreference
import com.death14stroke.newsdaily.ui.viewmodel.CountriesViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class CountryPreferenceDialogFragment : PreferenceDialogFragmentCompat() {
    private var _binding: PrefDialogCountryBinding? = null
    private val binding: PrefDialogCountryBinding
        get() = _binding!!
    private val viewModel by viewModel<CountriesViewModel>()
    private val countryAdapter by lazy { CountryAdapter(countryClickListener) }
    private val countryClickListener: CountryClickListener = { country ->
        countryCode = country.alpha2.lowercase(Locale.ENGLISH)
        dialog?.dismiss()
        onDialogClosed(true)
    }

    private lateinit var countryCode: String

    companion object {
        fun newInstance(key: String) =
            CountryPreferenceDialogFragment().apply {
                arguments = bundleOf(ARG_KEY to key)
            }
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        _binding = PrefDialogCountryBinding.bind(view)
        initRecyclerView()

        countryCode = viewModel.getSelectedCountry()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.countriesFlow.collectLatest { countries ->
                    countryAdapter.submitList(countries)
                }
            }
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        _binding = null

        if (positiveResult) {
            val preference = preference
            if (preference is CountryPreference)
                preference.country = countryCode
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            adapter = countryAdapter
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }
}
