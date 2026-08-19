package com.floatshield.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Auto Ad-Blocker Injection for YouTube & Browsing
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectAdBlocker();
            }
        });

        webView.loadUrl("https://m.youtube.com");
    }

    private void injectAdBlocker() {
        String js = "javascript:(function() {" +
                "var style = document.createElement('style');" +
                "style.innerHTML = 'ytm-promoted-sparkles-web-renderer, ytm-companion-ad-renderer, .ytp-ad-overlay-container { display: none !important; }';" +
                "document.head.appendChild(style);" +
                "setInterval(function() {" +
                "  var btn = document.querySelector('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .ytm-ad-skip-button');" +
                "  if(btn) btn.click();" +
                "}, 500);" +
                "})()";
        webView.evaluateJavascript(js, null);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
