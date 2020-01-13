package com.andruid.magic.newsdaily.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.data.Constants
import com.andruid.magic.newsdaily.databinding.FragmentNewsBinding
import com.andruid.magic.newsdaily.eventbus.NewsEvent
import com.andruid.magic.newsdaily.ui.adapter.NewsAdapter
import com.andruid.magic.newsdaily.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.newsdaily.ui.viewmodel.NewsViewModel
import com.andruid.magic.newsloader.model.News
import com.yuyakaido.android.cardstackview.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class NewsFragment : Fragment() {
    private lateinit var binding: FragmentNewsBinding
    private lateinit var viewModel: NewsViewModel

    private val newsAdapter = NewsAdapter()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { safeArgs = NewsFragmentArgs.fromBundle(it) }
        newsAdapter.registerAdapterDataObserver(adapterObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        newsAdapter.unregisterAdapterDataObserver(adapterObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_news, container, false)
        setupCardStackView()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        safeArgs?.let {
            viewModel = ViewModelProvider(this, BaseViewModelFactory {
                NewsViewModel(it.category, requireActivity().application)
            }).get(NewsViewModel::class.java)
            viewModel.getNews().observe(this, Observer { news ->
                newsAdapter.submitList(news) {
                    if (!news.isEmpty())
                        hideProgress()
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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewsEvent(newsEvent: NewsEvent) {
        when (newsEvent.action) {
            Constants.ACTION_SHARE_NEWS -> shareNews(newsEvent.news)
            Constants.ACTION_OPEN_URL -> loadUrl(newsEvent.news.url)
        }
    }

    private fun loadUrl(url: String) {
        val directions = NewsFragmentDirections.actionNewsToWebview(url)
        findNavController().navigate(directions)
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
}