package com.death14stroke.newsdaily.ui.fragment

import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.death14stroke.newsdaily.R
import com.death14stroke.newsdaily.data.ACTION_PREPARE_AUDIO
import com.death14stroke.newsdaily.data.EXTRA_CATEGORY
import com.death14stroke.newsdaily.data.model.onLoading
import com.death14stroke.newsdaily.data.model.onSuccess
import com.death14stroke.newsdaily.databinding.FragmentNewsBinding
import com.death14stroke.newsdaily.service.AudioNewsService
import com.death14stroke.newsdaily.ui.adapter.NewsAdapter
import com.death14stroke.newsdaily.ui.custom.AlphaPageTransformer
import com.death14stroke.newsdaily.ui.util.getOpenNewsListener
import com.death14stroke.newsdaily.ui.util.getShareNewsListener
import com.death14stroke.newsdaily.ui.util.getViewImageListener
import com.death14stroke.newsdaily.ui.viewbinding.viewBinding
import com.death14stroke.newsdaily.ui.viewmodel.NewsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class NewsFragment : Fragment(R.layout.fragment_news),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val binding by viewBinding(FragmentNewsBinding::bind)
    private val safeArgs by navArgs<NewsFragmentArgs>()
    private val viewModel by viewModel<NewsViewModel> { parametersOf(safeArgs.category) }
    private val newsAdapter by lazy {
        NewsAdapter(
            getViewImageListener(),
            getOpenNewsListener(),
            getShareNewsListener()
        )
    }
    private val mediaControllerCallback = MediaControllerCallback()
    private val mediaBrowserCompat by lazy {
        MediaBrowserCompat(
            requireContext(),
            ComponentName(requireContext(), AudioNewsService::class.java),
            MBConnectionCallback(), null
        )
    }
    private var syncWithUI = false

    private lateinit var mediaControllerCompat: MediaControllerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        preferences.registerOnSharedPreferenceChangeListener(this@NewsFragment)

        syncWithUI = preferences.getBoolean(getString(R.string.pref_ui_sync), true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::mediaControllerCompat.isInitialized)
            mediaControllerCompat.unregisterCallback(mediaControllerCallback)
        //TODO: remove mediaBrowserCompat
        if (mediaBrowserCompat.isConnected)
            mediaBrowserCompat.disconnect()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPager()
        initListeners()
        mediaBrowserCompat.connect()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.newsFlow.collectLatest { result ->
                    result.onSuccess { data ->
                        data?.let { pagingData -> newsAdapter.submitData(lifecycle, pagingData) }
                        binding.progressBar.hide()
                    }.onLoading {
                        binding.progressBar.show()
                    }
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        if (getString(R.string.pref_country) == s) {
            val country = sharedPreferences.getString(s, viewModel.countryFlow.value)!!
            viewModel.countryFlow.value = country
        } else if (getString(R.string.pref_ui_sync) == s) {
            syncWithUI = sharedPreferences.getBoolean(s, false)
        }
    }

    /**
     * Util to scroll to the current playing news in the swipe cards
     */
    private fun scrollToCurrentNews(title: String) {
        newsAdapter.snapshot().items.apply {
            try {
                val optionalInt = IntRange(0, size - 1)
                    .firstOrNull { pos: Int -> this[pos].title == title }
                binding.viewPager.currentItem = optionalInt ?: return@apply
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

                mediaControllerCompat.metadata?.let { metadata ->
                    mediaControllerCallback.onMetadataChanged(metadata)
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