package com.andruid.magic.newsdaily.activity

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.data.AppConstants
import com.andruid.magic.newsdaily.databinding.ActivityWebViewBinding
import com.andruid.magic.newsdaily.util.MyWebChromeClient

class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewBinding
    private lateinit var url : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_web_view)

        intent.extras?.apply {
            url = getString(AppConstants.EXTRA_NEWS_URL) ?: ""
            setWebView()
        }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = url
        }
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
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    view.loadUrl(request.url.toString())
                    return true
                }
            }
            loadUrl(url)
            settings.apply {
                javaScriptEnabled = true
                builtInZoomControls = true
                useWideViewPort = true
                loadWithOverviewMode = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_web, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.menu_open_browser -> {
                val intent = Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse(url))
                startActivity(intent)
            }
            R.id.menu_copy -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("news", url)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Copied url", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }
}