package com.floatshield.mobile;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayInputStream;

public class MainActivity extends Activity {
    private WebView webView;
    private LinearLayout homeLayout;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable adBlockRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#090D16"));

        // Glassmorphic Home UI
        homeLayout = new LinearLayout(this);
        homeLayout.setOrientation(LinearLayout.VERTICAL);
        homeLayout.setGravity(Gravity.CENTER);
        homeLayout.setPadding(60, 100, 60, 100);
        homeLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        TextView title = new TextView(this);
        title.setText("FloatShield");
        title.setTextSize(36);
        title.setTextColor(Color.parseColor("#38BDF8"));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("Native Dynamic Engine");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.parseColor("#64748B"));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 10, 0, 80);

        LinearLayout btnYouTube = createOptionCard("▶️  YouTube");
        LinearLayout btnYTMusic = createOptionCard("🎵  YouTube Music");

        btnYouTube.setOnClickListener(v -> animateAndLoad(v, "https://m.youtube.com"));
        btnYTMusic.setOnClickListener(v -> animateAndLoad(v, "https://music.youtube.com"));

        homeLayout.addView(title);
        homeLayout.addView(subtitle);
        homeLayout.addView(btnYouTube);
        homeLayout.addView(btnYTMusic);

        // Advanced WebView Setup
        webView = new WebView(this);
        webView.setVisibility(View.GONE);
        webView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        
        // Force Standard Mobile Chrome UA with Anti-Ad-Check Headers
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {

            // Service Worker Interception (Disable background Ad Caching)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString().toLowerCase();
                if (url.contains("sw.js") || url.contains("serviceworker") || url.contains("/api/stats/ads") || url.contains("doubleclick.net") || url.contains("googleads")) {
                    return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                applyCSSAndScriptInjection();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                applyCSSAndScriptInjection();
            }
        });

        root.addView(homeLayout);
        root.addView(webView);
        setContentView(root);

        // Ultra-Fast 150ms Persistent Scanner Loop
        adBlockRunnable = new Runnable() {
            @Override
            public void run() {
                if (webView.getVisibility() == View.VISIBLE) {
                    applyCSSAndScriptInjection();
                }
                handler.postDelayed(this, 150);
            }
        };
        handler.post(adBlockRunnable);
    }

    private LinearLayout createOptionCard(String title) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(50, 45, 50, 45);

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(35);
        shape.setColor(Color.parseColor("#131C2E"));
        shape.setStroke(3, Color.parseColor("#1E293B"));
        card.setBackground(shape);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, 40);
        card.setLayoutParams(params);

        TextView label = new TextView(this);
        label.setText(title);
        label.setTextSize(18);
        label.setTextColor(Color.WHITE);
        label.setTypeface(null, android.graphics.Typeface.BOLD);

        card.addView(label);
        return card;
    }

        private void injectPayloadHijack() {
        String js = "javascript:(function() {" +
                "  try {" +
                // Bypass Visibility Check (Background Play Fix)
                "    Object.defineProperty(document, 'hidden', { value: false, writable: true });" +
                "    Object.defineProperty(document, 'visibilityState', { value: 'visible', writable: true });" +
                "    document.addEventListener('visibilitychange', function(e) { e.stopImmediatePropagation(); }, true);" +
                "    window.addEventListener('visibilitychange', function(e) { e.stopImmediatePropagation(); }, true);" +
                "    if (!window.fs_hijacked) {" +
                "      window.fs_hijacked = true;" +
                "      const origJSON = JSON.parse;" +
                "      JSON.parse = function(text) {" +
                "        let data = origJSON(text);" +
                "        if (data && data.adPlacements) { delete data.adPlacements; }" +
                "        return data;" +
                "      };" +
                "    }" +
                "    if (!document.getElementById('floatshield-badge')) {" +
                "      var badge = document.createElement('div');" +
                "      badge.id = 'floatshield-badge';" +
                "      badge.style.cssText = 'position:fixed!important;bottom:20px!important;right:20px!important;background:rgba(15,23,42,0.9)!important;color:#38BDF8!important;padding:8px 16px!important;border-radius:30px!important;font-size:12px!important;font-weight:bold!important;z-index:2147483647!important;border:1px solid #38BDF8!important;pointer-events:none!important;';" +
                "      badge.innerHTML = '🛡️ Background Enabled';" +
                "      (document.body || document.documentElement).appendChild(badge);" +
                "    }" +
                "    var adSelectors = ['.ad-showing', '.ad-interrupting', 'ytm-promoted-sparkles-web-renderer', 'ytm-companion-ad-renderer', 'ytm-statement-banner-renderer', 'ad-slot-renderer', '.ytp-ad-overlay-container'];" +
                "    adSelectors.forEach(function(s) { document.querySelectorAll(s).forEach(function(el) { el.remove(); }); });" +
                "    var video = document.querySelector('video');" +
                "    var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .ytm-ad-skip-button');" +
                "    if (skipBtn) { skipBtn.click(); }" +
                "    if (video && video.paused) { video.play(); }" +
                "  } catch(e) {}" +
                "})();";

        webView.post(() -> webView.evaluateJavascript(js, null));
    }
