package com.hufi.sensor;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;

public class Maps extends AppCompatActivity {
    private WebView webView;
    // Geolocation permission request code
    private static final int RP_ACCESS_LOCATION = 1001;

    // global variables for the origin for permission and interface used by the your application to set the Geolocation permission state for an origin
    private String mGeolocationOrigin;
    private GeolocationPermissions.Callback mGeolocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        webView=findViewById(R.id.webViewMap);
        //webView.setWebViewClient(new WebViewClient());
        webView.setWebViewClient(client);
        webView.getSettings().setJavaScriptEnabled(true);
        registerForContextMenu(webView);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.setWebChromeClient(new GeoWebChromeClient());
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl("https://www.google.com/maps");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RP_ACCESS_LOCATION:
                boolean allow = false;
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // user has allowed these permissions
                    allow = true;
                }
                if (mGeolocationCallback != null) {
                    // use stored callback and origin for allowing Geolocation permission for WebView
                    mGeolocationCallback.invoke(mGeolocationOrigin, allow, false);
                }
                break;
        }
    }

    public class GeoWebChromeClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            final String permission = Manifest.permission.ACCESS_FINE_LOCATION;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                    ContextCompat.checkSelfPermission(Maps.this, permission) == PackageManager.PERMISSION_GRANTED) {
                // that is you already implement, but it works only
                // we're on SDK < 23 OR user has ALREADY granted permission
                callback.invoke(origin, true, false);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(Maps.this, permission)) {
                    // user has denied this permission before and selected [/] DON'T ASK ME AGAIN
                    // TODO Best Practice: show an AlertDialog explaining why the user could allow this permission, then ask again
                } else {
                    // store
                    mGeolocationOrigin = origin;
                    mGeolocationCallback = callback;
                    // ask the user for permissions
                    ActivityCompat.requestPermissions(Maps.this, new String[] {permission}, RP_ACCESS_LOCATION);
                }
            }
        }
    }

    private final WebViewClient client = new WebViewClient() {
        public void onPageFinished(WebView view, String url) {
            //Toast.makeText(Maps.this, url, Toast.LENGTH_SHORT).show();
            capture();
        }
    };

    public void capture() {
        Bitmap bmp = null;
        ByteArrayOutputStream bos = null;
        byte[] bt = null;
        String image = "";
        try {
            bmp = Bitmap.createBitmap(webView.getWidth(),
                    webView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bmp);
            webView.draw(c);        //With quality value 100 still works on main page google.com, just too heavy cause exception e or simply not load, so change quality compress (default: 100)

            bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 0, bos);
            bt = bos.toByteArray();

            image = Base64.encodeToString(bt, Base64.DEFAULT);

            Intent intentWidget = new Intent(getApplicationContext(), SpeedometerWidget.class);
            intentWidget.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

            intentWidget.putExtra("webView", image);

            int[] ids = AppWidgetManager.getInstance(getApplicationContext()).getAppWidgetIds(new ComponentName(getApplicationContext(), SpeedometerWidget.class));
            if(ids != null && ids.length > 0) {
                intentWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                sendBroadcast(intentWidget);
            }
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e){}
    }
}
