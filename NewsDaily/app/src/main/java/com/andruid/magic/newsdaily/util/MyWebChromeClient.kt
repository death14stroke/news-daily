package com.andruid.magic.newsdaily.util

import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ProgressBar

class MyWebChromeClient(val progressBar: ProgressBar) : WebChromeClient() {
    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        progressBar.progress = newProgress
        if (newProgress == 100)
            progressBar.visibility = View.GONE
        else
            progressBar.visibility = View.VISIBLE
    }
}