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
        subtitle.setText("Background Playback Enabled");
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

        webView = new WebView(this);
        webView.setVisibility(View.GONE);
        webView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString().toLowerCase();
                if (url.contains("sw.js") || url.contains("doubleclick.net") || url.contains("googleads")) {
                    return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
                }
                return super.shouldInterceptRequest(view, request);
            }
        });

        root.addView(homeLayout);
        root.addView(webView);
        setContentView(root);

        adBlockRunnable = new Runnable() {
            @Override
            public void run() {
                if (webView.getVisibility() == View.VISIBLE) {
                    injectPayloadHijack();
                }
                handler.postDelayed(this, 300);
            }
        };
        handler.post(adBlockRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onResume(); 
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
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

    private void injectPayloadHijack() {
        String js = "javascript:(function() {" +
                "  try {" +
                "    Object.defineProperty(document, 'hidden', { value: false, writable: true });" +
                "    Object.defineProperty(document, 'visibilityState', { value: 'visible', writable: true });" +
                "    var video = document.querySelector('video');" +
                "    var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytm-ad-skip-button');" +
                "    if (skipBtn) skipBtn.click();" +
                "    if (video && video.paused) video.play();" +
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
