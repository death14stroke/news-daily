package com.andruid.magic.newsdaily.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.database.entity.NewsItem
import com.andruid.magic.newsdaily.databinding.FragmentNewsBinding
import com.andruid.magic.newsdaily.ui.adapter.NewsAdapter
import com.andruid.magic.newsdaily.ui.custom.AlphaPageTransformer
import com.andruid.magic.newsdaily.ui.custom.CustomTabHelper
import com.andruid.magic.newsdaily.ui.viewmodel.BaseViewModelFactory
import com.andruid.magic.newsdaily.ui.viewmodel.NewsViewModel
import com.andruid.magic.newsdaily.util.color

class NewsFragment : Fragment(), NewsAdapter.NewsClickListener {
    private val safeArgs by navArgs<NewsFragmentArgs>()
    private val newsAdapter by lazy { NewsAdapter(this) }
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
        binding.viewPager.apply {
            adapter = newsAdapter
            setPageTransformer(AlphaPageTransformer())
        }
    }

    override fun onShareNews(news: NewsItem) {
        ShareCompat.IntentBuilder.from(requireActivity())
            .setSubject(news.title)
            .setText(news.url)
            .setType("text/plain")
            .setChooserTitle("Share this news with...")
            .startChooser()
    }

    override fun onOpenNews(url: String) {
        val builder = CustomTabsIntent.Builder()
            .setToolbarColor(color(R.color.colorPrimary))
            .setSecondaryToolbarColor(color(R.color.colorAccent))
            .addDefaultShareMenuItem()
            .setShowTitle(true)
            .setStartAnimations(requireContext(), R.anim.slide_in_right, R.anim.slide_out_left)
            .setExitAnimations(requireContext(), R.anim.slide_in_left, R.anim.slide_out_right)

        val packageName = CustomTabHelper.getPackageNameToUse(requireContext(), url)
        if (packageName != null) {
            val customTabsIntent = builder.build().also { it.intent.setPackage(packageName) }
            customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
        } else {
            val directions = NewsFragmentDirections.actionNewsToWebview(url)
            findNavController().navigate(directions)
        }
    }
}