package com.floatshield.mobile;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.ByteArrayInputStream;

public class MainActivity extends Activity {
    private WebView webView;
    private LinearLayout homeLayout;
    private MediaSession mediaSession;
    private boolean isAdBlockingEnabled = true;
    private int adBlockedCount = 0;
    private TextView counterText;
    private TextView toggleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Native Background Media Session
        mediaSession = new MediaSession(this, "FloatShieldSession");
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() { webView.evaluateJavascript("document.querySelector('video')?.play()", null); }
            @Override
            public void onPause() { webView.evaluateJavascript("document.querySelector('video')?.pause()", null); }
        });
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE)
                .setState(PlaybackState.STATE_PLAYING, 0, 1.0f).build());
        mediaSession.setActive(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#090D16"));

        homeLayout = new LinearLayout(this);
        homeLayout.setOrientation(LinearLayout.VERTICAL);
        homeLayout.setGravity(Gravity.CENTER);
        homeLayout.setPadding(50, 60, 50, 60);

        TextView title = new TextView(this);
        title.setText("FloatShield");
        title.setTextSize(32);
        title.setTextColor(Color.parseColor("#38BDF8"));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        counterText = new TextView(this);
        counterText.setText("🛡️ Ads Blocked: 0");
        counterText.setTextSize(14);
        counterText.setTextColor(Color.parseColor("#22C55E"));
        counterText.setPadding(0, 10, 0, 30);
        counterText.setGravity(Gravity.CENTER);

        toggleBtn = new TextView(this);
        toggleBtn.setTextSize(13);
        toggleBtn.setTextColor(Color.WHITE);
        toggleBtn.setPadding(35, 20, 35, 20);
        toggleBtn.setGravity(Gravity.CENTER);
        updateToggleStyle();

        toggleBtn.setOnClickListener(v -> {
            isAdBlockingEnabled = !isAdBlockingEnabled;
            updateToggleStyle();
        });

        LinearLayout spacer = new LinearLayout(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(-1, 60));

        // Native Canvas-based Logo Buttons
        View btnYouTube = createNativeLogoButton("YouTube", new YouTubeLogoDrawable(), "#180A0A", "#FF0000", "https://m.youtube.com");
        View btnYTMusic = createNativeLogoButton("YouTube Music", new YTMusicLogoDrawable(), "#181818", "#38BDF8", "https://music.youtube.com");

        homeLayout.addView(title);
        homeLayout.addView(counterText);
        homeLayout.addView(toggleBtn);
        homeLayout.addView(spacer);
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
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (!isAdBlockingEnabled) return super.shouldInterceptRequest(view, request);

                String url = request.getUrl().toString().toLowerCase();

                if (url.contains("sw.js") || url.contains("doubleclick.net") || url.contains("googleads") ||
                    url.contains("pagead") || url.contains("/api/stats/ads") || url.contains("/ptracking")) {
                    incrementAdCount();
                    return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                injectAdBlockEngine();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectAdBlockEngine();
            }
        });

        root.addView(homeLayout);
        root.addView(webView);
        setContentView(root);
    }

    private void incrementAdCount() {
        adBlockedCount++;
        new Handler(Looper.getMainLooper()).post(() -> counterText.setText("🛡️ Ads Blocked: " + adBlockedCount));
    }

    private void updateToggleStyle() {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(30);
        if (isAdBlockingEnabled) {
            toggleBtn.setText("⚡ Ad-Detector Engine: ACTIVE");
            shape.setColor(Color.parseColor("#0369A1"));
        } else {
            toggleBtn.setText("⚠️ Ad-Detector Engine: PAUSED");
            shape.setColor(Color.parseColor("#334155"));
        }
        toggleBtn.setBackground(shape);
    }

    private View createNativeLogoButton(String label, Drawable logoDrawable, String bgColor, String borderColor, String targetUrl) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(40, 30, 40, 30);

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(28);
        shape.setColor(Color.parseColor(bgColor));
        shape.setStroke(3, Color.parseColor(borderColor));
        card.setBackground(shape);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, 35);
        card.setLayoutParams(params);

        ImageView iconView = new ImageView(this);
        iconView.setLayoutParams(new LinearLayout.LayoutParams(70, 70));
        iconView.setImageDrawable(logoDrawable);

        TextView text = new TextView(this);
        text.setText(label);
        text.setTextSize(18);
        text.setTextColor(Color.WHITE);
        text.setTypeface(null, android.graphics.Typeface.BOLD);
        text.setPadding(30, 0, 0, 0);

        card.addView(iconView);
        card.addView(text);

        card.setOnClickListener(v -> {
            homeLayout.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(targetUrl);
        });

        return card;
    }

    private void injectAdBlockEngine() {
        String js = "javascript:(function() {" +
                "  try {" +
                "    Object.defineProperty(document, 'hidden', { value: false, writable: true });" +
                "    Object.defineProperty(document, 'visibilityState', { value: 'visible', writable: true });" +
                "    if (!window.fs_patched) {" +
                "      window.fs_patched = true;" +
                "      const cleanJSON = function(obj) {" +
                "        if (!obj) return obj;" +
                "        if (obj.adPlacements) delete obj.adPlacements;" +
                "        if (obj.playerAds) delete obj.playerAds;" +
                "        if (obj.adSlots) delete obj.adSlots;" +
                "        return obj;" +
                "      };" +
                "      const origParse = JSON.parse;" +
                "      JSON.parse = function(text) {" +
                "        let data = origParse(text);" +
                "        return cleanJSON(data);" +
                "      };" +
                "    }" +
                "    if (!document.getElementById('fs-css')) {" +
                "      var style = document.createElement('style');" +
                "      style.id = 'fs-css';" +
                "      style.innerHTML = 'ytm-promoted-sparkles-web-renderer, ytm-companion-ad-renderer, .ad-showing, .ad-interrupting, .ytp-ad-overlay-container, ad-slot-renderer, ytm-statement-banner-renderer, .ytp-ad-skip-button-slot { display: none !important; opacity: 0 !important; visibility: hidden !important; }';" +
                "      (document.head || document.documentElement).appendChild(style);" +
                "    }" +
                "    setInterval(function() {" +
                "      var video = document.querySelector('video');" +
                "      var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytm-ad-skip-button, .ytp-ad-skip-button-modern');" +
                "      if (skipBtn) { skipBtn.click(); }" +
                "      if (document.querySelector('.ad-showing, .ad-interrupting') && video) {" +
                "        video.muted = true;" +
                "        video.currentTime = (video.duration || 100) - 0.1;" +
                "      }" +
                "      if (video && video.paused && !video.ended) { video.play(); }" +
                "    }, 150);" +
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

    // Native YouTube Vector Drawable
    private static class YouTubeLogoDrawable extends Drawable {
        private final Paint redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public YouTubeLogoDrawable() {
            redPaint.setColor(Color.parseColor("#FF0000"));
            whitePaint.setColor(Color.WHITE);
        }

        @Override
        public void draw(Canvas canvas) {
            RectF bounds = new RectF(getBounds());
            float r = bounds.height() * 0.25f;
            canvas.drawRoundRect(bounds, r, r, redPaint);

            Path triangle = new Path();
            triangle.moveTo(bounds.left + bounds.width() * 0.38f, bounds.top + bounds.height() * 0.28f);
            triangle.lineTo(bounds.left + bounds.width() * 0.70f, bounds.top + bounds.height() * 0.50f);
            triangle.lineTo(bounds.left + bounds.width() * 0.38f, bounds.top + bounds.height() * 0.72f);
            triangle.close();
            canvas.drawPath(triangle, whitePaint);
        }

        @Override public void setAlpha(int alpha) { redPaint.setAlpha(alpha); }
        @Override public void setColorFilter(ColorFilter cf) { redPaint.setColorFilter(cf); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }

    // Native YouTube Music Vector Drawable
    private static class YTMusicLogoDrawable extends Drawable {
        private final Paint redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public YTMusicLogoDrawable() {
            redPaint.setColor(Color.parseColor("#FF0000"));
            whitePaint.setColor(Color.WHITE);
        }

        @Override
        public void draw(Canvas canvas) {
            RectF bounds = new RectF(getBounds());
            float cx = bounds.centerX();
            float cy = bounds.centerY();
            float radius = Math.min(bounds.width(), bounds.height()) / 2f;

            canvas.drawCircle(cx, cy, radius, redPaint);
            canvas.drawCircle(cx, cy, radius * 0.6f, whitePaint);
            canvas.drawCircle(cx, cy, radius * 0.45f, redPaint);

            Path triangle = new Path();
            triangle.moveTo(cx - radius * 0.12f, cy - radius * 0.22f);
            triangle.lineTo(cx + radius * 0.22f, cy);
            triangle.lineTo(cx - radius * 0.12f, cy + radius * 0.22f);
            triangle.close();
            canvas.drawPath(triangle, whitePaint);
        }

        @Override public void setAlpha(int alpha) { redPaint.setAlpha(alpha); }
        @Override public void setColorFilter(ColorFilter cf) { redPaint.setColorFilter(cf); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}
