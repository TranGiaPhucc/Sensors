package com.hufi.sensor;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

public class Sensor2 extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor light;
    double lightValue = 0;

    public Sensor2() {
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

    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() != Sensor.TYPE_GRAVITY) return;
        //lightValue = Math.sqrt(Math.pow(event.values[0],2) + Math.pow(event.values[1],2) + Math.pow(event.values[2],2));    //linear acceleration
        //lightValue = event.values[1];     //Gravity

        Intent intent = this.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        lightValue = ((float) intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0)) / 10;

        showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        light = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        if (light != null) {
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                start();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void start()
    {
        showNotification();
        //mHandler.postDelayed(mRunnable, 1000);
    }
/*
    private final Runnable mRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void run() {

            showNotification();

            mHandler.postDelayed(mRunnable, 1000);
        }
    };*/

    private void showNotification() {
        // TODO Auto-generated method stub

        double value = (double)Math.round(lightValue * 10) / 10;
        String contentText = "Temperature: "  + value + " oC";

        Bitmap bitmap = createBitmapFromString(Double.toString(value), "oC");
        Icon icon = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            icon = Icon.createWithBitmap(bitmap);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My notification g", "My notification g", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setVibrationPattern(new long[]{ 0 });
            channel.enableVibration(false);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder noti = new NotificationCompat.Builder(this, "My notification g")
                    //.setContentTitle("Internet Speed Meter" + "     " + connectionType)
                    .setContentTitle("Battery Temperature")
                    .setContentText(contentText)
                    //builder.setSmallIcon(R.mipmap.ic_launcher_round);
                    .setSmallIcon(IconCompat.createFromIcon(icon))
                    .setAutoCancel(false)
                    .setOnlyAlertOnce(true);

            //notificationManager.notify(4, noti.build());

            startForeground(4, noti.build());
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

    private class mBatInfoReceiver extends BroadcastReceiver {

        int temp = 0;

        float get_temp(){
            return (float)(temp / 10);
        }

        @Override
        public void onReceive(Context arg0, Intent intent) {
            temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
        }
    };
}