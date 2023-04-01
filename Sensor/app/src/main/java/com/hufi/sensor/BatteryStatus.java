package com.hufi.sensor;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Timer;
import java.util.TimerTask;

public class BatteryStatus extends Service {

    private Handler mHandler = new Handler();

    public BatteryStatus() {
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

        //IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        //Intent batteryStatus = registerReceiver(null, ifilter);

        BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);

        int amp = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        int battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
        int level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        double time = -1;

        String chargeStr = "";
        if (amp >= 0) {
            chargeStr = "Discharge: ";
            time = (double) battery / amp * 60;
        }
        else {
            chargeStr = "Charge: ";
            double chargeLeft = (double) battery / level * 100 - battery;
            time = (double) -chargeLeft / amp * 60;
        }

        String batteryText = "Battery: " + amp / 1000 + " mA / " + (double)Math.round((double)battery / 1000 * 10) / 10 + " mAh\n" + chargeStr + Math.round(time) + " minutes";

        Intent intentWidget = new Intent(this, BatteryWidget.class);
        intentWidget.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intentWidget.putExtra("batteryText", batteryText);

        int[] ids = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, BatteryWidget.class));
        if(ids != null && ids.length > 0) {
            intentWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intentWidget);
        }

        Bitmap bitmap = createBitmapFromString(String.valueOf(amp / 1000), String.valueOf((double)Math.round((double)battery / 1000 * 10) / 10));
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
                    //.setContentTitle("Internet Speed Meter" + "     " + connectionType)
                    .setContentTitle("Current: " + amp / 1000 + " mA")
                    .setContentText("Battery left: " + (double)battery / 1000 + " mAh       " + chargeStr + Math.round(time) + " minutes")
                    //builder.setSmallIcon(R.mipmap.ic_launcher_round);
                    .setSmallIcon(IconCompat.createFromIcon(icon))
                    .setAutoCancel(false)
                    .setOnlyAlertOnce(true);

            //notificationManager.notify(1, noti.build());

            startForeground(7, noti.build());
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
}