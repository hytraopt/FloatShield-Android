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

public class MainActivity extends Activity {
    private WebView webView;
    private LinearLayout homeLayout;
    private MediaSession mediaSession;
    private boolean isAdBlockingEnabled = true;
    private int adBlockedCount = 0;
    private TextView counterText;
    private TextView toggleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // MediaSession Setup for Notification Controls
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
        root.setBackgroundColor(Color.parseColor("#0F0F0F"));

        homeLayout = new LinearLayout(this);
        homeLayout.setOrientation(LinearLayout.VERTICAL);
        homeLayout.setGravity(Gravity.CENTER);
        homeLayout.setPadding(40, 40, 40, 40);

        counterText = new TextView(this);
        counterText.setText("Ads Blocked: 0");
        counterText.setTextColor(Color.GREEN);
        counterText.setPadding(0, 0, 0, 20);
        homeLayout.addView(counterText);

        toggleText = new TextView(this);
        toggleText.setText("Ad-Blocker: ON");
        toggleText.setTextColor(Color.WHITE);
        toggleText.setPadding(0, 0, 0, 40);
        toggleText.setOnClickListener(v -> {
            isAdBlockingEnabled = !isAdBlockingEnabled;
            toggleText.setText(isAdBlockingEnabled ? "Ad-Blocker: ON" : "Ad-Blocker: OFF");
        });
        homeLayout.addView(toggleText);

        homeLayout.addView(createStyledButton("YouTube", "#FF0000", "https://m.youtube.com"));
        homeLayout.addView(createStyledButton("YouTube Music", "#1E1E1E", "https://music.youtube.com"));

        webView = new WebView(this);
        webView.setVisibility(View.GONE);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (!isAdBlockingEnabled) return super.shouldInterceptRequest(view, request);
                
                String url = request.getUrl().toString().toLowerCase();
                if (url.contains("doubleclick") || url.contains("pagead") || url.contains("googleads")) {
                    adBlockedCount++;
                    new Handler(Looper.getMainLooper()).post(() -> counterText.setText("Ads Blocked: " + adBlockedCount));
                    return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
        webView.setWebChromeClient(new WebChromeClient());

        root.addView(homeLayout);
        root.addView(webView);
        setContentView(root);
    }

    private LinearLayout createStyledButton(String text, String color, String url) {
        LinearLayout btn = new LinearLayout(this);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(60, 40, 60, 40);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(20);
        bg.setColor(Color.parseColor(color));
        btn.setBackground(bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 20, 0, 20);
        btn.setLayoutParams(params);
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(Color.WHITE);
        btn.addView(label);
        btn.setOnClickListener(v -> {
            homeLayout.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(url);
        });
        return btn;
    }

    @Override
    public void onBackPressed() {
        if (webView.getVisibility() == View.VISIBLE) {
            webView.setVisibility(View.GONE);
            homeLayout.setVisibility(View.VISIBLE);
        } else { super.onBackPressed(); }
    }
}
