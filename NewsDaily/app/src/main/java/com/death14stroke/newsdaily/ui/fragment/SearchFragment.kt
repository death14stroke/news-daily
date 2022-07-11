package com.death14stroke.newsdaily.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.death14stroke.newsdaily.R
import com.death14stroke.newsdaily.databinding.FragmentSearchBinding
import com.death14stroke.newsdaily.ui.activity.HomeActivity
import com.death14stroke.newsdaily.ui.adapter.NewsAdapter
import com.death14stroke.newsdaily.ui.custom.AlphaPageTransformer
import com.death14stroke.newsdaily.ui.util.getOpenNewsListener
import com.death14stroke.newsdaily.ui.util.getShareNewsListener
import com.death14stroke.newsdaily.ui.util.getViewImageListener
import com.death14stroke.newsdaily.ui.viewbinding.viewBinding
import com.death14stroke.newsdaily.ui.viewmodel.SearchViewModel
import com.death14stroke.newsdaily.data.model.onSuccess
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SearchFragment : Fragment(R.layout.fragment_search) {
    private val binding by viewBinding(FragmentSearchBinding::bind)
    private val searchViewModel by sharedViewModel<SearchViewModel>()
    private val newsAdapter by lazy {
        NewsAdapter(
            getViewImageListener(),
            getOpenNewsListener(),
            getShareNewsListener()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    (requireActivity() as HomeActivity).closeSearchView()
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPager()
        newsAdapter.addLoadStateListener { updateEmpty(newsAdapter.itemCount == 0) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchViewModel.articlesFlow.collectLatest { result ->
                    result.onSuccess { data ->
                        data?.let { pagingData -> newsAdapter.submitData(lifecycle, pagingData) }
                    }
                }
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

    private fun updateEmpty(empty: Boolean) {
        if (empty) {
            binding.emptyTV.isVisible = true
            binding.viewPager.isVisible = false
        } else {
            binding.emptyTV.isVisible = false
            binding.viewPager.isVisible = true
        }
    }
}