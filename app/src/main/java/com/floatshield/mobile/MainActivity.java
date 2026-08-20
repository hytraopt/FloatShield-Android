package com.floatshield.mobile;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    private WebView webView;
    private LinearLayout homeLayout;
    private MediaSession mediaSession;
    private boolean isAdBlockingEnabled = true;
    private int adBlockedCount = 0;
    private TextView counterText;
    private TextView toggleBtn;

    private static final String YT_LOGO_SVG = "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 576 512'><path fill='%23FF0000' d='M549.655 124.083c-6.281-23.65-24.787-42.276-48.284-48.597C458.781 64 288 64 288 64S117.22 64 74.629 75.486c-23.497 6.322-42.003 24.947-48.284 48.597-11.412 42.867-11.412 132.305-11.412 132.305s0 89.438 11.412 132.305c6.281 23.65 24.787 41.5 48.284 47.821C117.22 448 288 448 288 448s170.78 0 213.371-11.486c23.497-6.321 42.003-24.171 48.284-47.821 11.412-42.867 11.412-132.305 11.412-132.305s0-89.438-11.412-132.305zm-317.51 213.508V175.185l142.739 81.205-142.739 81.201z'/></svg>";
    private static final String YTM_LOGO_SVG = "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 512 512'><path fill='%23FF0000' d='M256 8C119 8 8 119 8 256s111 248 248 248 248-111 248-248S393 8 256 8zm0 388c-77.3 0-140-62.7-140-140S178.7 116 256 116s140 62.7 140 140-62.7 140-140 140zm0-220c-44.2 0-80 35.8-80 80s35.8 80 80 80 80-35.8 80-80-35.8-80-80-80zm0 112c-17.7 0-32-14.3-32-32s14.3-32 32-32 32 14.3 32 32-14.3 32-32 32z'/></svg>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mediaSession = new MediaSession(this, "FloatShieldSession");
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() { webView.evaluateJavascript("document.querySelector('video').play()", null); }
            @Override
            public void onPause() { webView.evaluateJavascript("document.querySelector('video').pause()", null); }
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
        toggleBtn.setText("Ad-Detector Engine: ON");
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

        View btnYouTube = createLogoButton("YouTube", YT_LOGO_SVG, "#180A0A", "#FF0000", "https://m.youtube.com");
        View btnYTMusic = createLogoButton("YouTube Music", YTM_LOGO_SVG, "#181818", "#38BDF8", "https://music.youtube.com");

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
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36");

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (!isAdBlockingEnabled) return super.shouldInterceptRequest(view, request);

                String url = request.getUrl().toString().toLowerCase();

                // 1. Direct Block Ad Network Domains
                if (url.contains("doubleclick.net") || url.contains("googleads") || url.contains("pagead") ||
                    url.contains("/api/stats/ads") || url.contains("/ptracking")) {
                    incrementAdCount();
                    return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
                }

                // 2. Intercept and Strip YouTube API JSON Payload Ads
                if (url.contains("/youtubei/v1/player") || url.contains("/youtubei/v1/next")) {
                    try {
                        incrementAdCount();
                        WebResourceResponse response = fetchAndStripAds(request);
                        if (response != null) return response;
                    } catch (Exception e) {}
                }

                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                injectClientEngine();
                super.onPageFinished(view, url);
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

    private WebResourceResponse fetchAndStripAds(WebResourceRequest request) {
        try {
            URL url = new URL(request.getUrl().toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(request.getMethod());

            for (Map.Entry<String, String> header : request.getRequestHeaders().entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }

            InputStream in = conn.getInputStream();
            byte[] buffer = new byte[in.available()];
            in.read(buffer);
            String json = new String(buffer, "UTF-8");

            // Deep JSON Payload Scrubbing
            json = json.replaceAll("\"adPlacements\":\\[.*?\\]", "\"adPlacements\":[]");
            json = json.replaceAll("\"adSlots\":\\[.*?\\]", "\"adSlots\":[]");
            json = json.replaceAll("\"playerAds\":\\[.*?\\]", "\"playerAds\":[]");

            return new WebResourceResponse("application/json", "UTF-8", new ByteArrayInputStream(json.getBytes("UTF-8")));
        } catch (Exception e) {
            return null;
        }
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

    private View createLogoButton(String label, String svgData, String bgColor, String borderColor, String targetUrl) {
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

        WebView iconView = new WebView(this);
        iconView.setLayoutParams(new LinearLayout.LayoutParams(90, 90));
        iconView.setBackgroundColor(Color.TRANSPARENT);
        iconView.getSettings().setJavaScriptEnabled(false);
        String html = "<html><body style='margin:0;padding:0;background:transparent;display:flex;justify-content:center;align-items:center;'><img src='" + svgData + "' width='32' height='32'/></body></html>";
        iconView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

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

    private void injectClientEngine() {
        String js = "javascript:(function() {" +
                "  Object.defineProperty(document, 'hidden', { value: false, writable: true });" +
                "  Object.defineProperty(document, 'visibilityState', { value: 'visible', writable: true });" +
                "  if (!document.getElementById('fs-css')) {" +
                "    var style = document.createElement('style');" +
                "    style.id = 'fs-css';" +
                "    style.innerHTML = '.ad-showing, .ad-interrupting, ytm-promoted-sparkles-web-renderer, ytm-companion-ad-renderer, ytm-statement-banner-renderer, ad-slot-renderer, .ytp-ad-overlay-container { display: none !important; opacity: 0 !important; }';" +
                "    (document.head || document.documentElement).appendChild(style);" +
                "  }" +
                "  setInterval(function() {" +
                "    var video = document.querySelector('video');" +
                "    var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytm-ad-skip-button, .ytp-ad-skip-button-modern');" +
                "    if (skipBtn) skipBtn.click();" +
                "    if (document.querySelector('.ad-showing video') && video) {" +
                "      video.muted = true;" +
                "      video.currentTime = (video.duration || 100) - 0.1;" +
                "    }" +
                "    if (video && video.paused && !video.ended) video.play();" +
                "  }, 200);" +
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
