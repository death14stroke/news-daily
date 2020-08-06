package com.andruid.magic.newsdaily.ui.custom

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ProgressBar
import com.andruid.magic.newsdaily.util.hide
import com.andruid.magic.newsdaily.util.show

class ProgressWebChromeClient(private val progressBar: ProgressBar) : WebChromeClient() {
    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)

        progressBar.progress = newProgress
        if (newProgress == 100)
            progressBar.hide()
        else
            progressBar.show()
    }
}