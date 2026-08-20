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
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
    private WebView webView;
    private LinearLayout homeLayout;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable adBlockRunnable;
    private int blockedCount = 0;

    // Known YouTube Ad Networks & In-Stream Tracking Endpoints
    private static final List<String> AD_DOMAINS = Arrays.asList(
            "doubleclick.net", "googleadservices.com", "googlesyndication.com",
            "/pagead/", "/ptracking", "/api/stats/ads", "pubads",
            "ad_status", "adunit", "googleads"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#090D16"));

        // Dashboard Home UI
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
        subtitle.setText("Deep Ad-Intercept Engine");
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

        // Advanced Network Intercepting WebView Engine
        webView = new WebView(this);
        webView.setVisibility(View.GONE);
        webView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {

            // Network-Level Ad Dropper
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString().toLowerCase();
                for (String domain : AD_DOMAINS) {
                    if (url.contains(domain)) {
                        blockedCount++;
                        // Return empty response to drop the connection completely
                        return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                runClientDomPurge();
            }
        });

        root.addView(homeLayout);
        root.addView(webView);
        setContentView(root);

        // Multi-Layer Runtime Enforcement Loop
        adBlockRunnable = new Runnable() {
            @Override
            public void run() {
                if (webView.getVisibility() == View.VISIBLE) {
                    runClientDomPurge();
                }
                handler.postDelayed(this, 400); // Fast 400ms scan
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

    private void runClientDomPurge() {
        // Deep JS Purge Engine with Muted Speed Fast-Forwarding
        String js = "javascript:(function() {" +
                "  try {" +
                "    if (!document.getElementById('floatshield-badge')) {" +
                "      var badge = document.createElement('div');" +
                "      badge.id = 'floatshield-badge';" +
                "      badge.style.cssText = 'position:fixed!important;bottom:20px!important;right:20px!important;background:rgba(15,23,42,0.9)!important;color:#38BDF8!important;padding:8px 16px!important;border-radius:30px!important;font-size:12px!important;font-weight:bold!important;z-index:2147483647!important;border:1px solid #38BDF8!important;pointer-events:none!important;';" +
                "      badge.innerHTML = '🛡️ Shield: <span id=\"shield-count\">" + blockedCount + "</span> Blocked';" +
                "      (document.body || document.documentElement).appendChild(badge);" +
                "    } else {" +
                "      document.getElementById('shield-count').innerText = '" + blockedCount + "';" +
                "    }" +
                "    var adSelectors = ['.ad-showing', '.ad-interrupting', 'ytm-promoted-sparkles-web-renderer', 'ytm-companion-ad-renderer', 'ytm-statement-banner-renderer', 'ad-slot-renderer', '.ytp-ad-overlay-container'];" +
                "    adSelectors.forEach(function(s) {" +
                "      var els = document.querySelectorAll(s);" +
                "      els.forEach(function(el) { el.remove(); });" +
                "    });" +
                "    var video = document.querySelector('video');" +
                "    var isAdPlaying = document.querySelector('.ad-showing, .ad-interrupting');" +
                "    var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .ytm-ad-skip-button');" +
                "    if (skipBtn) { skipBtn.click(); }" +
                "    if (isAdPlaying && video) {" +
                "      video.muted = true;" +
                "      video.playbackRate = 16.0;" +
                "      if (!isNaN(video.duration) && video.duration > 0) { video.currentTime = video.duration - 0.1; }" +
                "    }" +
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
