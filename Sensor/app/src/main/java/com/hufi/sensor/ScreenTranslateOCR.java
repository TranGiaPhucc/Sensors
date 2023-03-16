package com.hufi.sensor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ScreenTranslateOCR extends Service {
    private Handler mHandler = new Handler();

    public ScreenTranslateOCR() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        start();
        showNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    private void start() {
        mHandler.postDelayed(mRunnable, 5000);
    }

    private final Runnable mRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void run() {
            sendMessage();
            mHandler.postDelayed(mRunnable, 5000);
        }
    };

    private void sendMessage() {
        // The string "my-message" will be used to filer the intent
        Intent intent = new Intent("ocr");
        // Adding some data
        //intent.putExtra("txBytes", txBytes);
        //intent.putExtra("rxBytes", rxBytes);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void showNotification() {
        // TODO Auto-generated method stub

        String title = "Screenshot Translate OCR";
        String contentText = "This service is capturing your device and translate it.";

        Bitmap bitmap = createBitmapFromString("o", "OCR");
        Icon icon = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            icon = Icon.createWithBitmap(bitmap);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My notification d", "My notification d", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setVibrationPattern(new long[]{ 0 });
            channel.enableVibration(false);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder noti = new NotificationCompat.Builder(this, "My notification d")
                    //.setContentTitle("Internet Speed Meter" + "     " + connectionType)
                    .setContentTitle(title)
                    .setContentText(contentText)
                    //builder.setSmallIcon(R.mipmap.ic_launcher_round);
                    .setSmallIcon(IconCompat.createFromIcon(icon))
                    .setAutoCancel(false)
                    .setOnlyAlertOnce(true);

            //notificationManager.notify(3, noti.build());

            startForeground(5, noti.build());
        }
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
}