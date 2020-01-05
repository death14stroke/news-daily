package com.andruid.magic.newsdaily.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.adapter.NewsAdapter
import com.andruid.magic.newsdaily.articles.ArticlesViewModel
import com.andruid.magic.newsdaily.databinding.ActivitySearchBinding
import com.andruid.magic.newsdaily.util.BaseViewModelFactory
import com.andruid.magic.newsdaily.util.RxSearchObservable.Companion.fromView
import com.yuyakaido.android.cardstackview.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var disposable: Disposable
    private lateinit var articlesViewModel: ArticlesViewModel
    private lateinit var cardStackLayoutManager: CardStackLayoutManager

    private var newsAdapter: NewsAdapter = NewsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)
        cardStackLayoutManager = CardStackLayoutManager(this, object : CardStackListener {
            override fun onCardDragging(direction: Direction, ratio: Float) {}
            override fun onCardSwiped(direction: Direction) {}
            override fun onCardRewound() {}
            override fun onCardCanceled() {}
            override fun onCardAppeared(view: View, position: Int) {}
            override fun onCardDisappeared(view: View, position: Int) {}
        })
        articlesViewModel = ViewModelProvider(this, BaseViewModelFactory {
            ArticlesViewModel("en") }).get(ArticlesViewModel::class.java)
        disposable = fromView(binding.searchView)
                .debounce(1000, TimeUnit.MILLISECONDS)
                .filter { text: String -> text.isNotEmpty() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { query: String? -> loadArticles(query ?: "") }

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setUpCardStackView()

        articlesViewModel.pagedListLiveData.observe(this, Observer {
            newsAdapter.submitList(it)
        })
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
        binding.cardStackView.apply {
            layoutManager = cardStackLayoutManager
            adapter = newsAdapter
        }
    }

    fun loadArticles(query : String) {
        articlesViewModel.setQuery(query)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val item = menu.findItem(R.id.searchItem)
        binding.searchView.setMenuItem(item)
        binding.searchView.showSearch(true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
        disposable.dispose()
    }

    override fun onBackPressed() {
        if (binding.searchView.isSearchOpen)
            binding.searchView.closeSearch()
        else
            super.onBackPressed()
    }
}