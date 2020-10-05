package com.example.test.sub;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test.R;

public class SubActivityImg extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_show_img);

        final Button logoutButton = findViewById(R.id.back);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                finish();
            }

        });
        WebView webView = findViewById(R.id.webview_sub);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(getString(R.string.BaseUrl)+getString(R.string.ViewImg));

    }
}

