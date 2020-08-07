package com.andruid.magic.newsdaily.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.data.ACTION_SEARCH_ARTICLES
import com.andruid.magic.newsdaily.data.EXTRA_QUERY
import com.andruid.magic.newsdaily.database.entity.NewsItem
import com.andruid.magic.newsdaily.databinding.FragmentSearchBinding
import com.andruid.magic.newsdaily.ui.activity.HomeActivity
import com.andruid.magic.newsdaily.ui.adapter.NewsAdapter
import com.andruid.magic.newsdaily.ui.custom.AlphaPageTransformer
import com.andruid.magic.newsdaily.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.newsdaily.ui.viewmodel.SearchViewModel
import com.andruid.magic.newsdaily.util.hide
import com.andruid.magic.newsdaily.util.openChromeCustomTab
import com.andruid.magic.newsdaily.util.shareNews
import com.andruid.magic.newsdaily.util.show

class SearchFragment : Fragment(), NewsAdapter.NewsClickListener {
    private val newsAdapter by lazy { NewsAdapter(this) }
    private val searchViewModel by viewModels<SearchViewModel> {
        BaseViewModelFactory { SearchViewModel() }
    }
    private val queryBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_SEARCH_ARTICLES) {
                val query = intent.extras!!.getString(EXTRA_QUERY, "")

                Log.d("searchLog", "query = $query")

                searchViewModel.setQuery(query)
            }
        }
    }

    private lateinit var binding: FragmentSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        requireActivity().onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (requireActivity() as HomeActivity).closeSearchView()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        newsAdapter.addLoadStateListener { loadState ->
            updateEmpty(newsAdapter.itemCount == 0)
        }

        initViewPager()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            queryBroadcastReceiver, IntentFilter(ACTION_SEARCH_ARTICLES)
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(queryBroadcastReceiver)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        menu.findItem(R.id.action_intro)?.isVisible = false
        menu.findItem(R.id.action_settings)?.isVisible = false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        searchViewModel.articles.observe(viewLifecycleOwner, Observer { articles ->
            Log.d("searchLog", "search results found")
            newsAdapter.submitData(lifecycle, articles)
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

    private fun updateEmpty(empty: Boolean) {
        if (empty) {
            binding.emptyTV.show()
            binding.viewPager.hide()
        } else {
            binding.emptyTV.hide()
            binding.viewPager.show()
        }
    }
}