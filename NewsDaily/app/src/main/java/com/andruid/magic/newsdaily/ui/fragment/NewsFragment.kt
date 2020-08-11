package com.andruid.magic.newsdaily.ui.fragment

import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.PagingData
import androidx.preference.PreferenceManager
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.data.ACTION_PREPARE_AUDIO
import com.andruid.magic.newsdaily.data.EXTRA_CATEGORY
import com.andruid.magic.newsdaily.database.entity.NewsItem
import com.andruid.magic.newsdaily.databinding.FragmentNewsBinding
import com.andruid.magic.newsdaily.service.AudioNewsService
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
        BaseViewModelFactory { NewsViewModel(requireContext().getSelectedCountry(), safeArgs.category) }
    }
    private val mediaControllerCallback = MediaControllerCallback()
    private val mediaBrowserCompat by lazy {
        MediaBrowserCompat(
            requireContext(), ComponentName(
                requireContext(),
                AudioNewsService::class.java
            ), MBConnectionCallback(), null
        )
    }
    private var syncWithUI = false

    private lateinit var mediaControllerCompat: MediaControllerCompat
    private lateinit var binding: FragmentNewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.registerOnSharedPreferenceChangeListener(this@NewsFragment)

        syncWithUI = preferences.getBoolean(getString(R.string.pref_ui_sync), false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewsBinding.inflate(inflater, container, false)

        initViewPager()
        initListeners()
        mediaBrowserCompat.connect()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaControllerCompat.unregisterCallback(mediaControllerCallback)
        mediaBrowserCompat.disconnect()
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

    override fun onShareNews(news: NewsItem) {
        requireActivity().shareNews(news)
    }

    override fun onOpenNews(url: String) {
        requireContext().openChromeCustomTab(url) {
            val directions = NewsFragmentDirections.actionNewsToWebview(url)
            findNavController().navigate(directions)
        }
    }

    override fun onViewImage(view: View, imageUrl: String) {
        val directions = NewsFragmentDirections.actionNewsToShowImage(imageUrl)
        val extras = FragmentNavigatorExtras(
            view to view.transitionName
        )
        findNavController().navigate(directions, extras)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        if (getString(R.string.pref_country) == s) {
            val country = sharedPreferences.getString(s, requireContext().getSelectedCountry())!!
            WorkerScheduler.scheduleNewsWorker(requireContext())
            newsViewModel.updateCountry(country)
        }
        if (getString(R.string.pref_ui_sync) == s)
            syncWithUI = sharedPreferences.getBoolean(s, false)
    }

    private fun scrollToCurrentNews(title: String) {
        val newsList = newsAdapter.snapshot().items
        newsList.apply {
            try {
                val optionalInt =
                    IntRange(0, size - 1).firstOrNull { pos: Int -> this[pos].title == title }
                binding.viewPager.currentItem = optionalInt ?: 0
            } catch (e: NoSuchElementException) {
                e.printStackTrace()
            }
        }
    }

    private fun initViewPager() {
        binding.viewPager.apply {
            adapter = newsAdapter
            setPageTransformer(AlphaPageTransformer())

            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
    }

    private fun initListeners() {
        binding.speakBtn.setOnClickListener {
            val intent = Intent(requireContext(), AudioNewsService::class.java)
                .setAction(ACTION_PREPARE_AUDIO)
                .putExtra(EXTRA_CATEGORY, safeArgs.category)
            requireContext().startService(intent)
        }
    }

    private inner class MBConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            try {
                mediaControllerCompat =
                    MediaControllerCompat(requireContext(), mediaBrowserCompat.sessionToken).apply {
                        registerCallback(mediaControllerCallback)
                    }
                MediaControllerCompat.setMediaController(requireActivity(), mediaControllerCompat)

                mediaControllerCompat.metadata?.apply {
                    mediaControllerCallback.onMetadataChanged(
                        this
                    )
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)?.let {
                if (syncWithUI)
                    scrollToCurrentNews(it)
            }
        }
    }
}