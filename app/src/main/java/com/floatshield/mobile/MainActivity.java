package com.floatshield.mobile;

import android.app.Activity;
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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        // 1. Futuristic Glassmorphic Dashboard UI
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
        subtitle.setText("Ad-Free Streaming Engine");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.parseColor("#64748B"));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 10, 0, 80);

        // Official Red & Pink Accent Platform Cards
        LinearLayout btnYouTube = createOptionCard("YouTube", "#FF0000", "https://upload.wikimedia.org/wikipedia/commons/e/ef/Youtube_logo.png");
        LinearLayout btnYTMusic = createOptionCard("YouTube Music", "#FF0055", "https://upload.wikimedia.org/wikipedia/commons/6/6a/Youtube_Music_icon.svg");

        btnYouTube.setOnClickListener(v -> animateAndLoad(v, "https://m.youtube.com"));
        btnYTMusic.setOnClickListener(v -> animateAndLoad(v, "https://music.youtube.com"));

        homeLayout.addView(title);
        homeLayout.addView(subtitle);
        homeLayout.addView(btnYouTube);
        homeLayout.addView(btnYTMusic);

        // 2. Android 16 Stricter-Security Compatible WebView Engine
        webView = new WebView(this);
        webView.setVisibility(View.GONE);
        webView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        
        // Android 16 Chrome Modern Desktop/Mobile UserAgent Bypass
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 16; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                forceInjectEngine();
            }
        });

        root.addView(homeLayout);
        root.addView(webView);
        setContentView(root);

        // Continuous Loop Optimized for Android 16 Lifecycle
        adBlockRunnable = new Runnable() {
            @Override
            public void run() {
                if (webView.getVisibility() == View.VISIBLE) {
                    forceInjectEngine();
                }
                handler.postDelayed(this, 500); // Faster 0.5s checks
            }
        };
        handler.post(adBlockRunnable);
    }

    private LinearLayout createOptionCard(String title, String accentColor, String logoUrl) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
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

    private void animateAndLoad(View view, String url) {
        ScaleAnimation anim = new ScaleAnimation(1f, 0.94f, 1f, 0.94f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(120);
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);
        view.startAnimation(anim);

        handler.postDelayed(() -> {
            homeLayout.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(url);
        }, 150);
    }

    private void forceInjectEngine() {
        // Universal Native & Dynamic JS Injector for Android 16 Security
        String js = "javascript:(function() {" +
                "  try {" +
                "    if (!document.getElementById('floatshield-badge')) {" +
                "      var badge = document.createElement('div');" +
                "      badge.id = 'floatshield-badge';" +
                "      badge.style.cssText = 'position:fixed!important;bottom:24px!important;right:20px!important;background:rgba(15,23,42,0.92)!important;backdrop-filter:blur(12px)!important;color:#38BDF8!important;padding:10px 18px!important;border-radius:40px!important;font-family:sans-serif!important;font-size:13px!important;font-weight:800!important;z-index:2147483647!important;border:1.5px solid #38BDF8!important;pointer-events:none!important;box-shadow:0 8px 24px rgba(0,0,0,0.5)!important;';" +
                "      badge.innerHTML = '🛡️ Shield: <span id=\"shield-count\">0</span> Blocked';" +
                "      (document.body || document.documentElement).appendChild(badge);" +
                "    }" +
                "    var countEl = document.getElementById('shield-count');" +
                "    var count = parseInt(countEl ? countEl.innerText : '0');" +
                "    var selectors = ['ytm-promoted-sparkles-web-renderer', 'ytm-companion-ad-renderer', '.ytp-ad-overlay-container', 'ytm-statement-banner-renderer', 'div[class*=\"ad-slot\"]', 'ad-slot-renderer', 'ytm-promoted-video-renderer', 'ytm-inline-ad-renderer', 'div[class*=\"ad-showing\"]'];" +
                "    selectors.forEach(function(s) {" +
                "      var els = document.querySelectorAll(s);" +
                "      els.forEach(function(el) {" +
                "        if(el && el.style.display !== 'none') { el.style.setProperty('display', 'none', 'important'); count++; }" +
                "      });" +
                "    });" +
                "    var video = document.querySelector('video');" +
                "    var isAd = document.querySelector('.ad-showing, .ad-interrupting, .ytp-ad-player-overlay, .ytm-ad-player-overlay');" +
                "    var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .ytm-ad-skip-button, .ytp-ad-skip-button-slot');" +
                "    if(skipBtn) { skipBtn.click(); count++; }" +
                "    if(isAd && video && !isNaN(video.duration) && video.duration > 0) { video.currentTime = video.duration - 0.1; count++; }" +
                "    if(countEl) countEl.innerText = count;" +
                "  } catch(e) {}" +
                "})();";
        
        webView.post(() -> webView.evaluateJavascript(js, null));
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
