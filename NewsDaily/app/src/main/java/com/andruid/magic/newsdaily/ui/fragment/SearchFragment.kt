package com.andruid.magic.newsdaily.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.data.Constants
import com.andruid.magic.newsdaily.databinding.FragmentSearchBinding
import com.andruid.magic.newsdaily.eventbus.NewsEvent
import com.andruid.magic.newsdaily.eventbus.SearchEvent
import com.andruid.magic.newsdaily.ui.adapter.NewsAdapter
import com.andruid.magic.newsdaily.ui.viewmodel.ArticlesViewModel
import com.andruid.magic.newsdaily.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.newsloader.model.NewsOnline
import com.yuyakaido.android.cardstackview.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var viewModel: ArticlesViewModel

    private val newsAdapter by lazy { NewsAdapter(requireActivity() as AppCompatActivity) }
    private val adapterObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            updateEmpty()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            super.onItemRangeChanged(positionStart, itemCount)
            updateEmpty()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            updateEmpty()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            updateEmpty()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        newsAdapter.registerAdapterDataObserver(adapterObserver)
        setHasOptionsMenu(true)
        retainInstance = true
    }

    override fun onDestroy() {
        super.onDestroy()
        newsAdapter.unregisterAdapterDataObserver(adapterObserver)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_intro)?.isVisible = false
        menu.findItem(R.id.action_settings)?.isVisible = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )
            : View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        setUpCardStackView()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
        viewModel.pos = (binding.cardStackView.layoutManager as CardStackLayoutManager).topPosition
        Log.d("newslog", "saving to viewModel ${viewModel.pos} for search")
    }

    private fun setUpCardStackView() {
        val cardStackLayoutManager = CardStackLayoutManager(context, object : CardStackListener {
            override fun onCardDragging(direction: Direction, ratio: Float) {}
            override fun onCardSwiped(direction: Direction) {}
            override fun onCardRewound() {}
            override fun onCardCanceled() {}
            override fun onCardAppeared(view: View, position: Int) {}
            override fun onCardDisappeared(view: View, position: Int) {}
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, BaseViewModelFactory { ArticlesViewModel() })
            .get(ArticlesViewModel::class.java)
        viewModel.searchLiveData.observe(this, Observer { news ->
            newsAdapter.submitList(news) { updateEmpty() }
            Log.d("newslog", "scrolling to ${viewModel.pos} for search")
            binding.cardStackView.scrollToPosition(viewModel.pos)
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSearchEvent(searchEvent: SearchEvent) {
        requireActivity().title = "Search for ${searchEvent.query}"
        viewModel.setQuery(searchEvent.query)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewsEvent(newsEvent: NewsEvent) {
        when (newsEvent.action) {
            Constants.ACTION_SHARE_NEWS -> shareNews(newsEvent.news)
            Constants.ACTION_OPEN_URL -> loadUrl(newsEvent.news.url)
        }
    }

    private fun loadUrl(url: String) {
        val directions = SearchFragmentDirections.actionNewsToWebview(url)
        findNavController().navigate(directions)
    }

    private fun updateEmpty() {
        if (newsAdapter.itemCount == 0) {
            binding.emptyTV.visibility = View.VISIBLE
            binding.cardStackView.visibility = View.GONE
        } else {
            binding.emptyTV.visibility = View.GONE
            binding.cardStackView.visibility = View.VISIBLE
        }
    }
}