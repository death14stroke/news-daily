package com.andruid.magic.newsdaily.ui.fragment


import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.databinding.FragmentWebViewBinding
import com.andruid.magic.newsdaily.ui.util.MyWebChromeClient

class WebViewFragment : Fragment() {
    private lateinit var binding: FragmentWebViewBinding
    private lateinit var safeArgs: WebViewFragmentArgs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { safeArgs = WebViewFragmentArgs.fromBundle(it) }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_web_view, container,
            false)
        setWebView()
        loadNewsUrl()

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigateUp()
        }
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_web, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_open_browser -> {
                val intent = Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse(safeArgs.newsUrl))
                startActivity(intent)
            }
            R.id.menu_copy -> {
                val clipboard = context!!.getSystemService(Context.CLIPBOARD_SERVICE)
                        as ClipboardManager
                val clip = ClipData.newPlainText("news", safeArgs.newsUrl)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied url", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }

    private fun loadNewsUrl() {
        binding.webView.loadUrl(safeArgs.newsUrl)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebView() {
        binding.webView.apply {
            webChromeClient = MyWebChromeClient(binding.progressBar)
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