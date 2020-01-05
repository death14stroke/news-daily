package com.andruid.magic.newsdaily.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.RemoteException
import android.speech.tts.TextToSpeech
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.adapter.NewsAdapter
import com.andruid.magic.newsdaily.data.AppConstants
import com.andruid.magic.newsdaily.databinding.ActivityMainBinding
import com.andruid.magic.newsdaily.eventbus.NewsEvent
import com.andruid.magic.newsdaily.headlines.NewsViewModel
import com.andruid.magic.newsdaily.service.AudioNewsService
import com.andruid.magic.newsdaily.util.BaseViewModelFactory
import com.andruid.magic.newsdaily.util.CategoryUtil.Companion.getCategories
import com.andruid.magic.newsdaily.util.PrefUtil.Companion.getDefaultCountry
import com.andruid.magic.newsloader.model.News
import com.cleveroad.loopbar.widget.OnItemClickListener
import com.yuyakaido.android.cardstackview.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity(), OnItemClickListener, OnSharedPreferenceChangeListener {
    companion object {
        const val MY_DATA_CHECK_CODE = 0
    }

    private lateinit var category : String
    private lateinit var categories : List<String>
    private lateinit var binding: ActivityMainBinding
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var cardStackLayoutManager: CardStackLayoutManager
    private lateinit var mediaBrowserCompat: MediaBrowserCompat
    private lateinit var mediaControllerCompat: MediaControllerCompat

    private var mediaControllerCallback: MediaControllerCallback = MediaControllerCallback()
    private var newsAdapter : NewsAdapter = NewsAdapter()
    private var syncWithUI = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.registerOnSharedPreferenceChangeListener(this)
        syncWithUI = preferences.getBoolean(getString(R.string.pref_ui_sync), false)

        val country = preferences.getString(getString(R.string.pref_country), getDefaultCountry(this))
        newsViewModel = ViewModelProvider(this, BaseViewModelFactory {
            NewsViewModel(country ?: "in") }).get(NewsViewModel::class.java)

        cardStackLayoutManager = CardStackLayoutManager(this, object : CardStackListener {
            override fun onCardDragging(direction: Direction, ratio: Float) {}
            override fun onCardSwiped(direction: Direction) {}
            override fun onCardRewound() {}
            override fun onCardCanceled() {}
            override fun onCardAppeared(view: View, position: Int) {}
            override fun onCardDisappeared(view: View, position: Int) {}
        })

        loadCategories()
        setUpCardStackView()

        binding.apply {
            loopBar.addOnItemClickListener(this@MainActivity)
            speakBtn.setOnClickListener {
                val checkTTSIntent = Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
                startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE)
            }
            cardStackView.setOnTouchListener { _, motionEvent ->
                val gestureDetector = GestureDetector(this@MainActivity,
                        object : SimpleOnGestureListener() {
                            override fun onDoubleTap(e: MotionEvent): Boolean {
                                if (loopBar.visibility == View.VISIBLE)
                                    loopBar.visibility = View.GONE
                                else
                                    loopBar.visibility = View.VISIBLE
                                return true
                            }
                        })
                gestureDetector.onTouchEvent(motionEvent)
            }
        }

        newsViewModel.pagedListLiveData.observe(this, Observer {
            newsAdapter.submitList(it)
        })

        val mbConnectionCallback = MBConnectionCallback()
        mediaBrowserCompat = MediaBrowserCompat(this, ComponentName(this,
                AudioNewsService::class.java), mbConnectionCallback, null)
        mediaBrowserCompat.connect()
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                val intent = Intent(this, AudioNewsService::class.java)
                        .putExtra(AppConstants.EXTRA_CATEGORY, category)
                intent.action = AppConstants.ACTION_PREPARE_AUDIO
                startService(intent)
            } else {
                val installTTSIntent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                startActivity(installTTSIntent)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewsEvent(newsEvent: NewsEvent) {
        val action = newsEvent.action
        if (AppConstants.ACTION_SHARE_NEWS == action)
            shareNews(newsEvent.news)
        else if (AppConstants.ACTION_OPEN_URL == action)
            loadUrl(newsEvent.news.url)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_search -> startActivity(Intent(this, SearchActivity::class.java))
            R.id.menu_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.menu_help -> startActivity(Intent(this, IntroActivity::class.java))
        }
        return true
    }

    private fun loadUrl(url: String) {
        val intent = Intent(this, WebViewActivity::class.java)
                .putExtra(AppConstants.EXTRA_NEWS_URL, url)
        startActivity(intent)
    }

    private fun shareNews(news: News) {
        val intent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, news.title)
                .putExtra(Intent.EXTRA_TEXT, news.url)
        startActivity(Intent.createChooser(intent, "Share news via..."))
    }

    private fun setUpCardStackView() {
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

        binding.cardStackView.layoutManager = cardStackLayoutManager
        binding.cardStackView.adapter = newsAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
        mediaControllerCompat.unregisterCallback(mediaControllerCallback)
        mediaBrowserCompat.disconnect()
    }

    override fun onItemClicked(position: Int) {
        category = categories[position]
        loadNews(category)
    }

    private fun loadNews(category : String) { newsViewModel.setCategory(category) }

    private fun loadCategories() {
        categories = getCategories()
        category = categories[0]
        loadNews(category)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        if (getString(R.string.pref_country) == s) {
            val country = sharedPreferences.getString(s, getDefaultCountry(this))
            newsViewModel.country = country ?: "in"
        }
        else if (getString(R.string.pref_ui_sync) == s)
            syncWithUI = sharedPreferences.getBoolean(s, false)
    }

    private fun scrollToCurrentNews(title: String) {
        val newsList = newsAdapter.getNewsList()
        newsList?.apply {
            try {
                val optionalInt = IntRange(0, size - 1).first { pos: Int -> this[pos].title == title }
                cardStackLayoutManager.scrollToPosition(optionalInt)
            } catch (e :NoSuchElementException) {
                e.printStackTrace()
            }
        }
    }

    inner class MBConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            try {
                mediaControllerCompat = MediaControllerCompat(this@MainActivity,
                        mediaBrowserCompat.sessionToken)
                mediaControllerCompat.registerCallback(mediaControllerCallback)
                MediaControllerCompat.setMediaController(this@MainActivity, mediaControllerCompat)
                mediaControllerCompat.metadata?.apply { mediaControllerCallback.onMetadataChanged(this) }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)?.apply {
                if (syncWithUI)
                    scrollToCurrentNews(this)
            }
        }
    }
}