package com.andruid.magic.newsdaily.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.andruid.magic.newsdaily.databinding.FragmentNewsBinding
import com.andruid.magic.newsdaily.ui.adapter.NewsAdapter
import com.andruid.magic.newsdaily.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.newsdaily.ui.viewmodel.NewsViewModel
import kotlin.math.abs
import kotlin.math.max

class NewsFragment : Fragment() {
    private val safeArgs by navArgs<NewsFragmentArgs>()
    private val newsAdapter by lazy { NewsAdapter(requireActivity() as AppCompatActivity) }
    private val newsViewModel by viewModels<NewsViewModel> {
        BaseViewModelFactory { NewsViewModel(safeArgs.category) }
    }

    private lateinit var binding: FragmentNewsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewsBinding.inflate(inflater, container, false)

        initViewPager()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        newsViewModel.news.observe(viewLifecycleOwner, Observer { news ->
            newsAdapter.submitData(lifecycle, news)
        })
    }

    private fun initViewPager() {
        binding.viewPager.adapter = newsAdapter

        val alphaPageTransformer = ViewPager2.PageTransformer { page, position ->
            page.apply {
                scaleX = max(0.9f, 1 - abs(position))
                scaleY = max(0.9f, 1 - abs(position))
            }

            when {
                position < -1 -> page.alpha = 0.1f
                position <= 1 -> page.alpha = max(0.2f, 1 - abs(position))
                else -> page.alpha = 0.1f
            }
        }
        binding.viewPager.setPageTransformer(alphaPageTransformer)
    }
}