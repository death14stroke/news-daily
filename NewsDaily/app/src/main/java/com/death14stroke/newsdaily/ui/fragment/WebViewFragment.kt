package com.death14stroke.newsdaily.ui.fragment

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.getSystemService
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.onNavDestinationSelected
import com.death14stroke.newsdaily.R
import com.death14stroke.newsdaily.databinding.FragmentWebViewBinding
import com.death14stroke.newsdaily.ui.custom.ProgressWebChromeClient
import com.death14stroke.newsdaily.ui.util.toast
import com.death14stroke.newsdaily.ui.viewbinding.viewBinding

class WebViewFragment : Fragment(R.layout.fragment_web_view) {
    private val binding by viewBinding(FragmentWebViewBinding::bind)
    private val safeArgs by navArgs<WebViewFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWebView()
        loadNewsUrl()
        setupOptionsMenu()
    }

    private fun setupOptionsMenu() {
        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_web, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.menu_open_browser -> {
                        val intent = Intent(Intent.ACTION_VIEW)
                            .setData(Uri.parse(safeArgs.newsUrl))
                        startActivity(intent)
                    }
                    R.id.menu_copy -> {
                        val clip = ClipData.newPlainText("news", safeArgs.newsUrl)
                        requireContext().getSystemService<ClipboardManager>()
                            ?.let { clipboardManager ->
                                clipboardManager.setPrimaryClip(clip)
                                toast(androidx.browser.R.string.copy_toast_msg)
                            } ?: run {
                            toast(R.string.error_copy_link)
                        }
                    }
                }
                val navController = findNavController()
                return menuItem.onNavDestinationSelected(navController)
            }
        }, viewLifecycleOwner)
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
            }
        }
    }
}