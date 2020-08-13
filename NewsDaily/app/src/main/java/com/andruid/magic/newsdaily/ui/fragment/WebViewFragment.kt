package com.andruid.magic.newsdaily.ui.fragment

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.databinding.FragmentWebViewBinding
import com.andruid.magic.newsdaily.ui.custom.ProgressWebChromeClient
import com.andruid.magic.newsdaily.util.toast

class WebViewFragment : Fragment() {
    private val safeArgs by navArgs<WebViewFragmentArgs>()

    private lateinit var binding: FragmentWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWebViewBinding.inflate(inflater, container, false)

        setWebView()
        loadNewsUrl()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_web, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_intro)?.isVisible = false
        menu.findItem(R.id.action_settings)?.isVisible = false
        menu.findItem(R.id.action_search).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_open_browser -> {
                val intent = Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse(safeArgs.newsUrl))
                startActivity(intent)
            }

            R.id.menu_copy -> {
                val clip = ClipData.newPlainText("news", safeArgs.newsUrl)
                requireContext().getSystemService<ClipboardManager>()?.let { clipboardManager ->
                    clipboardManager.setPrimaryClip(clip)
                    toast(R.string.copy_toast_msg)
                } ?: run {
                    toast(R.string.error_copy_link)
                }
            }
        }

        return NavigationUI.onNavDestinationSelected(item, findNavController())
                || super.onOptionsItemSelected(item)
    }

    private fun loadNewsUrl() {
        binding.webView.loadUrl(safeArgs.newsUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebView() {
        binding.webView.apply {
            webChromeClient = ProgressWebChromeClient(binding.progressBar)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    view.loadUrl(request.url.toString())
                    return true
                }
            }

            settings.apply {
                javaScriptEnabled = true
                builtInZoomControls = true
                useWideViewPort = true
                loadWithOverviewMode = true
                allowUniversalAccessFromFileURLs = true
            }
        }
    }
}