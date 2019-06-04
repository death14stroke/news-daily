package com.andruid.magic.newsdaily.util;

import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class MyWebChromeClient extends WebChromeClient {
    private ProgressBar progressBar;

    public MyWebChromeClient(ProgressBar progressBar){
        this.progressBar = progressBar;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        progressBar.setProgress(newProgress);
        if(newProgress == 100)
            progressBar.setVisibility(View.GONE);
        else
            progressBar.setVisibility(View.VISIBLE);
    }
}