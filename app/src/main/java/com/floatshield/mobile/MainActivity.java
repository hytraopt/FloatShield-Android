package com.floatshield.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private WebView webView;
    private LinearLayout homeLayout;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable adBlockRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Main Layout Container
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // 1. Home Selection Screen (YouTube / YouTube Music)
        homeLayout = new LinearLayout(this);
        homeLayout.setOrientation(LinearLayout.VERTICAL);
        homeLayout.setPadding(60, 100, 60, 100);
        homeLayout.setBackgroundColor(0xFF111827); // Dark Theme

        Button btnYouTube = createOptionButton("▶️ Open YouTube", "#FF0000");
        Button btnYTMusic = createOptionButton("🎵 Open YouTube Music", "#D97706");

        btnYouTube.setOnClickListener(v -> loadPlatform("https://m.youtube.com"));
        btnYTMusic.setOnClickListener(v -> loadPlatform("https://music.youtube.com"));

        homeLayout.addView(btnYouTube);
        homeLayout.addView(btnYTMusic);

        // 2. WebView Container
        webView = new WebView(this);
        webView.setVisibility(View.GONE);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setDatabaseEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectEngine();
            }
        });

        mainLayout.addView(homeLayout);
        mainLayout.addView(webView, new LinearLayout.LayoutParams(-1, -1));
        setContentView(mainLayout);

        // Background Ad-Block Engine
        adBlockRunnable = new Runnable() {
            @Override
            public void run() {
                if (webView.getVisibility() == View.VISIBLE) {
                    injectEngine();
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(adBlockRunnable);
    }

    private Button createOptionButton(String text, String colorHex) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(0xFFFFFFFF);
        btn.setTextSize(16);
        btn.setPadding(30, 40, 30, 40);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 20, 0, 20);
        btn.setLayoutParams(params);
        btn.setBackgroundColor(android.graphics.Color.parseColor(colorHex));
        return btn;
    }

    private void loadPlatform(String url) {
        homeLayout.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(url);
    }

    private void injectEngine() {
        String js = "javascript:(function() {" +
                "  if (!document.getElementById('floatshield-badge')) {" +
                "    var badge = document.createElement('div');" +
                "    badge.id = 'floatshield-badge';" +
                "    badge.style.cssText = 'position:fixed;bottom:20px;right:20px;background:#111827;color:#10B981;padding:8px 14px;border-radius:30px;font-family:sans-serif;font-size:12px;font-weight:bold;z-index:9999999;border:1px solid #10B981;pointer-events:none;';" +
                "    badge.innerHTML = '🛡️ FloatShield: <span id=\"shield-count\">0</span> Ads Blocked';" +
                "    document.body.appendChild(badge);" +
                "  }" +
                "  var countEl = document.getElementById('shield-count');" +
                "  var count = parseInt(countEl ? countEl.innerText : '0');" +
                "  var selectors = ['ytm-promoted-sparkles-web-renderer', 'ytm-companion-ad-renderer', '.ytp-ad-overlay-container', 'ytm-statement-banner-renderer', 'div[class*=\"ad-slot\"]'];" +
                "  selectors.forEach(function(s) {" +
                "    var els = document.querySelectorAll(s);" +
                "    els.forEach(function(el) {" +
                "      if(el && el.style.display !== 'none') { el.style.display = 'none'; count++; }" +
                "    });" +
                "  });" +
                "  var video = document.querySelector('video');" +
                "  var isAd = document.querySelector('.ad-showing, .ad-interrupting, .ytp-ad-player-overlay');" +
                "  var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .ytm-ad-skip-button, .ytp-ad-skip-button-slot');" +
                "  if(skipBtn) { skipBtn.click(); count++; }" +
                "  if(isAd && video && !isNaN(video.duration) && video.duration > 0) { video.currentTime = video.duration - 0.1; count++; }" +
                "  if(countEl) countEl.innerText = count;" +
                "})();";
        webView.evaluateJavascript(js, null);
    }

    @Override
    public void onBackPressed() {
        if (webView.getVisibility() == View.VISIBLE && webView.canGoBack()) {
            webView.goBack();
        } else if (webView.getVisibility() == View.VISIBLE) {
            webView.setVisibility(View.GONE);
            homeLayout.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
