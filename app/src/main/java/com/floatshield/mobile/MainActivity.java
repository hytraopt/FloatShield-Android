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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0F0F0F")); // YouTube Dark Theme Background

        homeLayout = new LinearLayout(this);
        homeLayout.setOrientation(LinearLayout.VERTICAL);
        homeLayout.setGravity(Gravity.CENTER);
        homeLayout.setPadding(40, 40, 40, 40);
        homeLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        TextView title = new TextView(this);
        title.setText("FloatShield Home");
        title.setTextSize(24);
        title.setTextColor(Color.WHITE);
        title.setPadding(0, 0, 0, 80);
        homeLayout.addView(title);

        // Improved Button UI
        homeLayout.addView(createStyledButton("YouTube", "#FF0000", "https://m.youtube.com"));
        homeLayout.addView(createStyledButton("YouTube Music", "#1E1E1E", "https://music.youtube.com"));

        webView = new WebView(this);
        webView.setVisibility(View.GONE);
        webView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36");

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString().toLowerCase();
                // Expanded Ad Block List
                if (url.contains("doubleclick") || url.contains("googleads") || url.contains("pagead") || 
                    url.contains("adservice") || url.contains("analytics") || url.contains("sclick")) {
                    return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                injectPayloadHijack();
                super.onPageFinished(view, url);
            }
        });

        root.addView(homeLayout);
        root.addView(webView);
        setContentView(root);
    }

    private LinearLayout createStyledButton(String text, String color, String url) {
        LinearLayout btn = new LinearLayout(this);
        btn.setOrientation(LinearLayout.VERTICAL);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(60, 40, 60, 40);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(20);
        bg.setColor(Color.parseColor(color));
        btn.setBackground(bg);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 20, 0, 20);
        btn.setLayoutParams(params);

        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(Color.WHITE);
        label.setTextSize(18);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        btn.addView(label);

        btn.setOnClickListener(v -> {
            homeLayout.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(url);
        });
        return btn;
    }

    private void injectPayloadHijack() {
        String js = "javascript:(function() {" +
                "  Object.defineProperty(document, 'hidden', { value: false, writable: true });" +
                "  var style = document.createElement('style');" +
                "  style.innerHTML = '.ad-showing, .ad-interrupting, ytm-promoted-sparkles-web-renderer { display:none !important; }';" +
                "  document.head.appendChild(style);" +
                "  setInterval(function() {" +
                "    var video = document.querySelector('video');" +
                "    if(video && video.paused) video.play();" +
                "    var skip = document.querySelector('.ytp-ad-skip-button, .ytm-ad-skip-button');" +
                "    if(skip) skip.click();" +
                "  }, 500);" +
                "})();";
        webView.evaluateJavascript(js, null);
    }

    @Override
    public void onBackPressed() {
        if (webView.getVisibility() == View.VISIBLE) {
            webView.setVisibility(View.GONE);
            homeLayout.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
