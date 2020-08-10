package com.andruid.magic.newsdaily.ui.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.PagingData
import com.andruid.magic.newsdaily.AudioNewsService
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.data.ACTION_PREPARE_AUDIO
import com.andruid.magic.newsdaily.data.EXTRA_CATEGORY
import com.andruid.magic.newsdaily.database.entity.NewsItem
import com.andruid.magic.newsdaily.databinding.FragmentNewsBinding
import com.andruid.magic.newsdaily.ui.adapter.NewsAdapter
import com.andruid.magic.newsdaily.ui.custom.AlphaPageTransformer
import com.andruid.magic.newsdaily.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.newsdaily.ui.viewmodel.NewsViewModel
import com.andruid.magic.newsdaily.util.getSelectedCountry
import com.andruid.magic.newsdaily.util.openChromeCustomTab
import com.andruid.magic.newsdaily.util.shareNews
import com.andruid.magic.newsdaily.worker.WorkerScheduler
import com.andruid.magic.newsloader.data.model.Result

class NewsFragment : Fragment(), NewsAdapter.NewsClickListener,
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val safeArgs by navArgs<NewsFragmentArgs>()
    private val newsAdapter by lazy { NewsAdapter(this) }
    private val newsViewModel by viewModels<NewsViewModel> {
        BaseViewModelFactory { NewsViewModel(requireActivity().application, safeArgs.category) }
    }

    private lateinit var binding: FragmentNewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewsBinding.inflate(inflater, container, false)

        binding.speakBtn.setOnClickListener {
            val intent = Intent(requireContext(), AudioNewsService::class.java)
                .setAction(ACTION_PREPARE_AUDIO)
                .putExtra(EXTRA_CATEGORY, "general")
            requireContext().startService(intent)
        }

        initViewPager()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        newsViewModel.newsLiveData.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Success<PagingData<NewsItem>> -> {
                    binding.progressBar.hide()
                    newsAdapter.submitData(lifecycle, result.data!!)
                }
                is Result.Error -> {
                }
                is Result.Loading -> {
                    binding.progressBar.show()
                }
            }
        })
    }

    private fun initViewPager() {
        binding.viewPager.apply {
            adapter = newsAdapter
            setPageTransformer(AlphaPageTransformer())
        }
    }

    override fun onShareNews(news: NewsItem) {
        requireActivity().shareNews(news)
    }

    override fun onOpenNews(url: String) {
        requireContext().openChromeCustomTab(url) {
            val directions = NewsFragmentDirections.actionNewsToWebview(url)
            findNavController().navigate(directions)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        if (getString(R.string.pref_country) == s) {
            val country = sharedPreferences.getString(s, requireContext().getSelectedCountry())!!
            WorkerScheduler.scheduleNewsWorker(requireContext())
            newsViewModel.updateCountry(country)
        }
        /*if (getString(R.string.pref_ui_sync) == s)
            val syncWithUI = sharedPreferences.getBoolean(s, false)*/
    }
}