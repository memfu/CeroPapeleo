package com.unirfp.ceropapeleo.web

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PdfWebViewScreen(url: String) {

    AndroidView(factory = { context ->

        WebView(context).apply {

            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()

            loadUrl(url)
        }
    })
}