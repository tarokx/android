package com.example.test.sub

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.test.R

class SubActivityImg : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sub_show_img)
        val logoutButton = findViewById<Button>(R.id.back)
        logoutButton.setOnClickListener { finish() }
        val webView = findViewById<WebView>(R.id.webview_sub)
        webView.webViewClient = WebViewClient()
        webView.loadUrl(getString(R.string.BaseUrl) + getString(R.string.ViewImg))
    }
}