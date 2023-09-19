package com.hufi.sensor;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.IconCompat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class BatteryTemperature extends Service {

    private Handler mHandler = new Handler();

    public BatteryTemperature() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.postDelayed(mRunnable, 1000);

        return super.onStartCommand(intent, flags, startId);
    }

    private final Runnable mRunnable = new Runnable() {
        public void run() {
            showNotification();

            mHandler.postDelayed(mRunnable, 1000);
        }
    };

    private void showNotification() {
        // TODO Auto-generated method stub


        Intent intent = this.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        double temp = ((float) intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0)) / 10;

        double value = (double)Math.round(temp * 10) / 10;
        String contentText = "Temperature: "  + value + " °C";

        Bitmap bitmap = createBitmapFromString(Integer.toString((int) value), "°C");
        Icon icon = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            icon = Icon.createWithBitmap(bitmap);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My notification", "My notification", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setVibrationPattern(new long[]{ 0 });
            channel.enableVibration(false);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder noti = new NotificationCompat.Builder(this, "My notification")
                    .setContentTitle("Battery Temperature")
                    .setContentText(contentText)
                    //builder.setSmallIcon(R.mipmap.ic_launcher_round);
                    .setSmallIcon(IconCompat.createFromIcon(icon))
                    .setAutoCancel(false)
                    .setOnlyAlertOnce(true);

            //notificationManager.notify(1, noti.build());

            startForeground(9, noti.build());
        }

        /*}
        else {
            stopForeground(true);

            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }(/

        /*NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(1, builder.build());*/
    }

    private Bitmap createBitmapFromString(String speed, String units) {

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(55);
        paint.setTextAlign(Paint.Align.CENTER);

        Paint unitsPaint = new Paint();
        unitsPaint.setAntiAlias(true);
        unitsPaint.setTextSize(40); // size is in pixels
        unitsPaint.setTextAlign(Paint.Align.CENTER);

        Rect textBounds = new Rect();
        paint.getTextBounds(speed, 0, speed.length(), textBounds);

        Rect unitsTextBounds = new Rect();
        unitsPaint.getTextBounds(units, 0, units.length(), unitsTextBounds);

        int width = (textBounds.width() > unitsTextBounds.width()) ? textBounds.width() : unitsTextBounds.width();

        Bitmap bitmap = Bitmap.createBitmap(width + 10, 90,
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(speed, width / 2 + 5, 50, paint);
        canvas.drawText(units, width / 2, 90, unitsPaint);

        return bitmap;
    }

    private static int readIntegerFile(String filePath) {

        try {
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filePath)), 1000);
            final String line = reader.readLine();
            reader.close();

            return Integer.parseInt(line);
        } catch (Exception e) {
            return 0;
        }
    }

    private static int takeCurrentCpuFreq(int coreIndex) {
        return readIntegerFile("/sys/devices/system/cpu/cpu" + coreIndex + "/cpufreq/scaling_cur_freq");
    }
}