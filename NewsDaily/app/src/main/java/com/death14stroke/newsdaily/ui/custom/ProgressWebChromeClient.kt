package com.death14stroke.newsdaily.ui.custom

import android.webkit.WebChromeClient
import android.webkit.WebView
import com.google.android.material.progressindicator.LinearProgressIndicator

class ProgressWebChromeClient(private val progressBar: LinearProgressIndicator) :
    WebChromeClient() {
    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)

        progressBar.progress = newProgress
        if (newProgress == 100)
            progressBar.hide()
        else
            progressBar.show()
    }
}