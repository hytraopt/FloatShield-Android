package com.floatshield.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable adBlockRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setDatabaseEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                injectEngine();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectEngine();
            }
        });

        webView.loadUrl("https://m.youtube.com");

        // Continuous Injector Loop for Dynamic Single Page Apps (SPA)
        adBlockRunnable = new Runnable() {
            @Override
            public void run() {
                injectEngine();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(adBlockRunnable);
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
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && adBlockRunnable != null) {
            handler.removeCallbacks(adBlockRunnable);
        }
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
