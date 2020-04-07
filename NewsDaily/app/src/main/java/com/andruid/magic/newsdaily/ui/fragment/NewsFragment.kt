package com.andruid.magic.newsdaily.ui.fragment

import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.RemoteException
import android.speech.tts.TextToSpeech
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.data.Constants
import com.andruid.magic.newsdaily.database.entity.News
import com.andruid.magic.newsdaily.databinding.FragmentNewsBinding
import com.andruid.magic.newsdaily.eventbus.NewsEvent
import com.andruid.magic.newsdaily.service.AudioNewsService
import com.andruid.magic.newsdaily.ui.adapter.NewsAdapter
import com.andruid.magic.newsdaily.ui.util.CustomTabHelper
import com.andruid.magic.newsdaily.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.newsdaily.ui.viewmodel.HeadlinesViewModel
import com.yuyakaido.android.cardstackview.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import splitties.resources.color

class NewsFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        private val TAG = "${NewsFragment::class.java.simpleName}log"
        private const val MY_DATA_CHECK_CODE = 0
    }

    private lateinit var binding: FragmentNewsBinding
    private lateinit var viewModel: HeadlinesViewModel
    private lateinit var mediaControllerCompat: MediaControllerCompat

    private val mediaControllerCallback = MediaControllerCallback()
    private val customTabHelper = CustomTabHelper()

    private val newsAdapter by lazy { NewsAdapter(requireActivity() as AppCompatActivity) }
    private val mediaBrowserCompat by lazy {
        MediaBrowserCompat(
            context, ComponentName(
                context!!,
                AudioNewsService::class.java
            ), MBConnectionCallback(), null
        )
    }

    private val adapterObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            if (newsAdapter.itemCount != 0)
                hideProgress()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            super.onItemRangeChanged(positionStart, itemCount)
            if (newsAdapter.itemCount != 0)
                hideProgress()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            if (newsAdapter.itemCount != 0)
                hideProgress()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            if (newsAdapter.itemCount != 0)
                hideProgress()
        }
    }

    private var safeArgs: NewsFragmentArgs? = null
    private var syncWithUI = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: news fragment")
        arguments?.let { safeArgs = NewsFragmentArgs.fromBundle(it)
            Log.d(TAG, "safeArgs: getting new safeArgs: ${safeArgs!!.category}");}
        newsAdapter.registerAdapterDataObserver(adapterObserver)

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.registerOnSharedPreferenceChangeListener(this@NewsFragment)

        syncWithUI = preferences.getBoolean(getString(R.string.pref_ui_sync), false)

        mediaBrowserCompat.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        newsAdapter.unregisterAdapterDataObserver(adapterObserver)
        mediaControllerCompat.unregisterCallback(mediaControllerCallback)
        mediaBrowserCompat.disconnect()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        if (getString(R.string.pref_ui_sync) == s)
            syncWithUI = sharedPreferences.getBoolean(s, false)
    }

    private fun scrollToCurrentNews(title: String) {
        val newsList = newsAdapter.getNewsList()
        newsList.apply {
            try {
                val optionalInt =
                    IntRange(0, size - 1).firstOrNull { pos: Int -> this[pos].title == title }
                binding.cardStackView.scrollToPosition(optionalInt ?: 0)
            } catch (e: NoSuchElementException) {
                e.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                val intent = Intent(context, AudioNewsService::class.java)
                    .putExtra(Constants.EXTRA_CATEGORY, safeArgs?.category ?: "general")
                intent.action = Constants.ACTION_PREPARE_AUDIO
                context?.startService(intent)
            } else {
                val installTTSIntent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                startActivity(installTTSIntent)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_news, container, false)
        binding.speakBtn.setOnClickListener {
            val checkTTSIntent = Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
            startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE)
        }
        setupCardStackView()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "onActivityCreated: news fragment")
        safeArgs?.let {
            Log.d(TAG, "onActivityCreated: category = ${it.category}")
            viewModel = ViewModelProvider(this, BaseViewModelFactory {
                HeadlinesViewModel(it.category, requireActivity().application)
            }).get(it.category, HeadlinesViewModel::class.java)
            viewModel.newsLiveData.observe(viewLifecycleOwner, Observer { news ->
                newsAdapter.submitList(news) {
                    if (!news.isEmpty())
                        hideProgress()
                    Log.d("newslog", "scrolling to ${viewModel.pos} for ${it.category}")
                    binding.cardStackView.scrollToPosition(viewModel.pos)
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
        viewModel.pos = (binding.cardStackView.layoutManager as CardStackLayoutManager).topPosition
        Log.d("newslog", "saving to viewModel ${viewModel.pos} for ${safeArgs?.category}")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewsEvent(newsEvent: NewsEvent) {
        when (newsEvent.action) {
            Constants.ACTION_SHARE_NEWS -> shareNews(newsEvent.news)
            Constants.ACTION_OPEN_URL -> loadUrl(newsEvent.news.url)
        }
    }

    private fun loadUrl(url: String) {
        val builder = CustomTabsIntent.Builder()
            .setToolbarColor(color(R.color.colorPrimary))
            .setSecondaryToolbarColor(color(R.color.colorAccent))
            .addDefaultShareMenuItem()
            .setShowTitle(true)
            .setStartAnimations(requireContext(), R.anim.slide_in_right, R.anim.slide_out_left)
            .setExitAnimations(requireContext(), R.anim.slide_in_left, R.anim.slide_out_right)
        val packageName = customTabHelper.getPackageNameToUse(requireContext(), url)

        if (packageName == null) {
            val directions = NewsFragmentDirections.actionNewsToWebview(url)
            findNavController().navigate(directions)
        } else {
            val customTabsIntent = builder.build()
            customTabsIntent.intent.setPackage(packageName)
            customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
        }
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
        binding.cardStackView.visibility = View.VISIBLE
    }

    private fun shareNews(news: News) {
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, news.title)
            .putExtra(Intent.EXTRA_TEXT, news.url)
        startActivity(Intent.createChooser(intent, "Share news via..."))
    }

    private fun setupCardStackView() {
        val cardStackLayoutManager = CardStackLayoutManager(context, object : CardStackListener {
            override fun onCardDisappeared(view: View?, position: Int) {}
            override fun onCardDragging(direction: Direction?, ratio: Float) {}
            override fun onCardSwiped(direction: Direction?) {}
            override fun onCardCanceled() {}
            override fun onCardAppeared(view: View?, position: Int) {}
            override fun onCardRewound() {}
        })
        cardStackLayoutManager.apply {
            val swipeSetting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Bottom)
                .setInterpolator(AccelerateInterpolator())
                .setDuration(Duration.Normal.duration)
                .build()
            setSwipeAnimationSetting(swipeSetting)
            setCanScrollHorizontal(false)
            setDirections(Direction.VERTICAL)
            setStackFrom(StackFrom.Bottom)
        }

        binding.cardStackView.apply {
            layoutManager = cardStackLayoutManager
            adapter = newsAdapter
        }
    }

    inner class MBConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            try {
                mediaControllerCompat =
                    MediaControllerCompat(context, mediaBrowserCompat.sessionToken)
                mediaControllerCompat.registerCallback(mediaControllerCallback)
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

    inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)?.let {
                if (syncWithUI)
                    scrollToCurrentNews(it)
            }
        }
    }
}