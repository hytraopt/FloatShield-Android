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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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

        // Frame Root Container
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0F172A")); // Premium Dark Slate Background

        // 1. Home Dashboard UI
        homeLayout = new LinearLayout(this);
        homeLayout.setOrientation(LinearLayout.VERTICAL);
        homeLayout.setGravity(Gravity.CENTER);
        homeLayout.setPadding(50, 80, 50, 80);
        homeLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        // Header Title
        TextView title = new TextView(this);
        title.setText("FloatShield");
        title.setTextSize(32);
        title.setTextColor(Color.parseColor("#38BDF8"));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("Select platform to start ad-free stream");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.parseColor("#94A3B8"));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 10, 0, 80);

        // Animated Cards
        LinearLayout btnYouTube = createOptionCard("▶️  YouTube", "#FF0000");
        LinearLayout btnYTMusic = createOptionCard("🎵  YouTube Music", "#FF0055");

        btnYouTube.setOnClickListener(v -> animateAndLoad(v, "https://m.youtube.com"));
        btnYTMusic.setOnClickListener(v -> animateAndLoad(v, "https://music.youtube.com"));

        homeLayout.addView(title);
        homeLayout.addView(subtitle);
        homeLayout.addView(btnYouTube);
        homeLayout.addView(btnYTMusic);

        // 2. Cross-Version Compatible WebView
        webView = new WebView(this);
        webView.setVisibility(View.GONE);
        webView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectEngine();
            }
        });

        root.addView(homeLayout);
        root.addView(webView);
        setContentView(root);

        // Universal Ad Block Engine Loop
        adBlockRunnable = new Runnable() {
            @Override
            public void run() {
                if (webView.getVisibility() == View.VISIBLE) {
                    injectEngine();
                }
                handler.postDelayed(this, 800);
            }
        };
        handler.post(adBlockRunnable);
    }

    private LinearLayout createOptionCard(String title, String accentColor) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(40, 45, 40, 45);

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(30);
        shape.setColor(Color.parseColor("#1E293B"));
        shape.setStroke(3, Color.parseColor("#334155"));
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
        ScaleAnimation anim = new ScaleAnimation(1f, 0.95f, 1f, 0.95f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(100);
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);
        view.startAnimation(anim);

        handler.postDelayed(() -> {
            homeLayout.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(url);
        }, 150);
    }

    private void injectEngine() {
        String js = "javascript:(function() {" +
                "  if (!document.getElementById('floatshield-badge')) {" +
                "    var badge = document.createElement('div');" +
                "    badge.id = 'floatshield-badge';" +
                "    badge.style.cssText = 'position:fixed;bottom:20px;right:20px;background:rgba(15,23,42,0.85);backdrop-filter:blur(10px);color:#38BDF8;padding:8px 16px;border-radius:30px;font-family:sans-serif;font-size:12px;font-weight:bold;z-index:9999999;border:1px solid #38BDF8;pointer-events:none;box-shadow:0 4px 12px rgba(0,0,0,0.3);';" +
                "    badge.innerHTML = '🛡️ Blocked: <span id=\"shield-count\">0</span>';" +
                "    document.body.appendChild(badge);" +
                "  }" +
                "  var countEl = document.getElementById('shield-count');" +
                "  var count = parseInt(countEl ? countEl.innerText : '0');" +
                "  var selectors = ['ytm-promoted-sparkles-web-renderer', 'ytm-companion-ad-renderer', '.ytp-ad-overlay-container', 'ytm-statement-banner-renderer', 'div[class*=\"ad-slot\"]', 'ad-slot-renderer', 'ytm-promoted-video-renderer'];" +
                "  selectors.forEach(function(s) {" +
                "    var els = document.querySelectorAll(s);" +
                "    els.forEach(function(el) {" +
                "      if(el && el.style.display !== 'none') { el.style.display = 'none'; count++; }" +
                "    });" +
                "  });" +
                "  var video = document.querySelector('video');" +
                "  var isAd = document.querySelector('.ad-showing, .ad-interrupting, .ytp-ad-player-overlay, .ytm-ad-player-overlay');" +
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
