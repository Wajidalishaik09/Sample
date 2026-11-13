package com.namaz100.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;

/** Hosts the Namaz 100 tracker (bundled in assets/www) in a full-screen WebView. */
public class MainActivity extends Activity {
    static final String HOST = "appassets.namaz100.local";
    static final String START = "https://" + HOST + "/namaz-tracker.html";
    static final int REQ_SAVE = 1, REQ_OPEN = 2, REQ_LOCATION = 3, REQ_NOTIFY = 4;

    private WebView web;
    private Store store;
    private String pendingSave;
    private ValueCallback<Uri[]> fileCallback;
    private GeolocationPermissions.Callback geoCallback;
    private String geoOrigin;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(Color.parseColor("#B88A2E"));
        getWindow().setNavigationBarColor(Color.parseColor("#0F3D2E"));

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#0F3D2E"));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setGeolocationEnabled(true);
        s.setAllowFileAccess(false);
        s.setMediaPlaybackRequiresUserGesture(true);
        s.setTextZoom(100);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest req) {
                Uri u = req.getUrl();
                if (!HOST.equals(u.getHost())) return null; // fonts etc. load normally
                String path = u.getPath();
                if (path == null || path.equals("/")) path = "/namaz-tracker.html";
                try {
                    InputStream in = getAssets().open("www" + path);
                    return new WebResourceResponse(mime(path), "utf-8", in);
                } catch (Exception e) {
                    return new WebResourceResponse("text/plain", "utf-8", 404, "Not Found", null, null);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri u = Uri.parse(url);
                if (HOST.equals(u.getHost())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) { }
                return true;
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback cb) {
                if (hasLocation()) { cb.invoke(origin, true, false); return; }
                geoOrigin = origin; geoCallback = cb;
                requestPermissions(new String[]{"android.permission.ACCESS_COARSE_LOCATION"}, REQ_LOCATION);
            }

            @Override
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = cb;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                try { startActivityForResult(Intent.createChooser(i, "Choose backup file"), REQ_OPEN); }
                catch (Exception e) { fileCallback = null; return false; }
                return true;
            }
        });

        try {
            store = (Store) Class.forName("com.namaz100.app.PlayStore").getConstructor(MainActivity.class).newInstance(this);
        } catch (Throwable t) { store = null; }  // not a Play build: everything unlocked
        web.addJavascriptInterface(new Bridge(), "AndroidBridge");
        if (saved != null) web.restoreState(saved);
        else web.loadUrl(START);

        // Android 15+ draws apps edge to edge: keep the page clear of the system bars,
        // and paint the bar areas gold (top, like the banner) and green (bottom, like the tabs).
        FrameLayout root = new FrameLayout(this);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#B88A2E"), Color.parseColor("#B88A2E"), Color.parseColor("#0F3D2E"), Color.parseColor("#0F3D2E")}));
        root.addView(web, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets in) {
                v.setPadding(in.getSystemWindowInsetLeft(), in.getSystemWindowInsetTop(),
                             in.getSystemWindowInsetRight(), in.getSystemWindowInsetBottom());
                return in;
            }
        });
        setContentView(root);
        root.requestApplyInsets();
    }

    static String mime(String p) {
        p = p.toLowerCase();
        if (p.endsWith(".html")) return "text/html";
        if (p.endsWith(".css")) return "text/css";
        if (p.endsWith(".js")) return "application/javascript";
        if (p.endsWith(".png")) return "image/png";
        if (p.endsWith(".svg")) return "image/svg+xml";
        if (p.endsWith(".woff2")) return "font/woff2";
        if (p.endsWith(".json") || p.endsWith(".webmanifest")) return "application/json";
        return "application/octet-stream";
    }

    boolean hasLocation() {
        return checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        if (code == REQ_LOCATION && geoCallback != null) {
            geoCallback.invoke(geoOrigin, hasLocation(), false);
            geoCallback = null;
        }
    }

    @Override
    protected void onActivityResult(int code, int result, Intent data) {
        if (code == REQ_SAVE) {
            if (result == RESULT_OK && data != null && data.getData() != null && pendingSave != null) {
                try {
                    OutputStream os = getContentResolver().openOutputStream(data.getData(), "wt");
                    os.write(pendingSave.getBytes("UTF-8"));
                    os.close();
                    Toast.makeText(this, "Backup saved", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Could not save the backup", Toast.LENGTH_LONG).show();
                }
            }
            pendingSave = null;
        } else if (code == REQ_OPEN && fileCallback != null) {
            fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result, data));
            fileCallback = null;
        }
    }

    /** Sends the purchase state to the page: window.onStoreState({...}). */
    void sendStoreState(final String json) {
        runOnUiThread(new Runnable() { public void run() {
            web.evaluateJavascript("window.onStoreState && window.onStoreState(" + json + ")", null);
        }});
    }

    static String jsonStr(String v) {
        if (v == null) return "\"\"";
        StringBuilder b = new StringBuilder("\"");
        for (char c : v.toCharArray()) {
            if (c == '"' || c == '\\') b.append('\\').append(c);
            else if (c < 0x20) b.append(String.format("\\u%04x", (int) c));
            else b.append(c);
        }
        return b.append('"').toString();
    }

    /** Methods callable from the page as window.AndroidBridge.* */
    class Bridge {
        @JavascriptInterface
        public boolean isPlayBuild() { return store != null; }

        @JavascriptInterface
        public void storeRefresh() { if (store != null) store.refresh(); else sendStoreState("{\"play\":false,\"owned\":true}"); }

        @JavascriptInterface
        public void storeBuy() { if (store != null) store.buy(); }

        @JavascriptInterface
        public void saveFile(final String name, final String text) {
            runOnUiThread(new Runnable() { public void run() {
                pendingSave = text;
                Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("application/json");
                i.putExtra(Intent.EXTRA_TITLE, name);
                try { startActivityForResult(i, REQ_SAVE); }
                catch (Exception e) { Toast.makeText(MainActivity.this, "No file app found", Toast.LENGTH_LONG).show(); }
            }});
        }

        @JavascriptInterface
        public void setReminders(final String json) {
            AlarmReceiver.store(MainActivity.this, json);
            AlarmReceiver.scheduleNext(MainActivity.this);
            if (json != null && json.length() > 2 && Build.VERSION.SDK_INT >= 33
                && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
                runOnUiThread(new Runnable() { public void run() {
                    requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, REQ_NOTIFY);
                }});
            }
        }
    }
}
