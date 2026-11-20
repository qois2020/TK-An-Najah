package id.annajah.guru;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.Locale;

/**
 * TK An Najah: aplikasi guru TK (assets/index.html) di dalam WebView.
 * Data tersimpan di HP (localStorage). Jembatan "AndroidVoice" untuk
 * membacakan cerita/arti doa dengan suara Bahasa Indonesia dan menyalin teks.
 */
public class MainActivity extends Activity {

    private WebView web;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private WebView printView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#FFF9EF"));
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        s.setTextZoom(100);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("file".equals(uri.getScheme())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (Exception ignored) { }
                return true;
            }
        });
        web.addJavascriptInterface(new VoiceBridge(), "AndroidVoice");
        web.addJavascriptInterface(new NotifyBridge(), "AndroidNotify");
        web.addJavascriptInterface(new PrintBridge(), "AndroidPrint");
        if (savedInstanceState != null) web.restoreState(savedInstanceState);
        else web.loadUrl("file:///android_asset/index.html");

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("id", "ID"));
                tts.setSpeechRate(0.92f);
                tts.setPitch(1.1f);
                ttsReady = true;
            }
        });

        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 9);
        }
        Reminders.scheduleAll(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        web.evaluateJavascript("(window.onAndroidBack&&window.onAndroidBack())?'1':'0'", v -> {
            if (v == null || !v.contains("1")) MainActivity.super.onBackPressed();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (tts != null) tts.stop();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) tts.shutdown();
        web.destroy();
        super.onDestroy();
    }

    class VoiceBridge {
        @JavascriptInterface
        public void speak(String text) {
            runOnUiThread(() -> {
                if (!ttsReady) {
                    Toast.makeText(MainActivity.this, "Suara belum siap. Pastikan mesin suara Google (Bahasa Indonesia) terpasang.", Toast.LENGTH_LONG).show();
                    return;
                }
                tts.stop();
                // Bacakan per kalimat agar teks panjang tidak terpotong.
                String[] parts = text.split("(?<=[.!?”])\\s+");
                for (int i = 0; i < parts.length; i++) {
                    tts.speak(parts[i], i == 0 ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD, null, "p" + i);
                }
            });
        }

        @JavascriptInterface
        public void stop() {
            runOnUiThread(() -> { if (tts != null) tts.stop(); });
        }

        @JavascriptInterface
        public void copy(String text) {
            runOnUiThread(() -> {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("TK An Najah", text));
            });
        }
    }

    class NotifyBridge {
        @JavascriptInterface
        public void sync(String json) {
            Reminders.sync(getApplicationContext(), json);
        }
    }

    class PrintBridge {
        @JavascriptInterface
        public void print(String html, String title) {
            runOnUiThread(() -> {
                final String job = (title == null || title.isEmpty()) ? "TK An Najah" : title;
                WebView pw = new WebView(MainActivity.this);
                pw.setWebViewClient(new WebViewClient() {
                    private boolean done = false;
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        if (done) return;
                        done = true;
                        PrintManager pm = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                        if (pm == null) return;
                        PrintDocumentAdapter ad = view.createPrintDocumentAdapter(job);
                        pm.print(job, ad, new PrintAttributes.Builder()
                                .setMediaSize(PrintAttributes.MediaSize.ISO_A4).build());
                    }
                });
                pw.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                printView = pw;
            });
        }
    }
}
