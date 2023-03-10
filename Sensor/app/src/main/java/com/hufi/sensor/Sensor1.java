package com.hufi.sensor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import java.util.Calendar;

public class Sensor1 extends Service implements LocationListener, GpsStatus.Listener {


    //private SensorManager sensorManager;
    //private Sensor light;
    //double vValue = 0;
    //double aValue = 0;
    //private double timestamp = 0;

    private int requestInterval = 100;      //Default: 1000 (1000ms = 1s)

    boolean gpsSatellites = false;

    LocationManager lm;

    //GPS Satellites
    int inUse = 0;
    int inView = 0;
    String strInUse = "Not used";
    String strInView = "Not connected";

    double speed = 0;
    double maxSpeed = 0;

    double length = 0;
    double totalSpeed = 0;
    double avgSpeed = 0;
    int countSpeed = 0;

    double curTime = 0;
    double oldLat = 0.0;
    double oldLon = 0.0;

    public Sensor1() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lm.removeUpdates(this);
        stopForeground(true);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /*@RequiresApi(api = Build.VERSION_CODES.M)
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION) return;
        aValue = Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));

        /*if (timestamp == 0) {
            timestamp = event.timestamp;
        }

        double dT = (event.timestamp - timestamp) / 1000000000;
        timestamp = event.timestamp;
        vValue = aValue * dT;//

        showNotification();
    }*/

    public void onGpsStatusChanged(int event) {
        getSatellitesCount();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        gpsSatellites = intent.getBooleanExtra("gpsSatellites", false);

        lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return super.onStartCommand(intent, flags, startId);
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, requestInterval, 0, this);

        if (gpsSatellites == true)
            lm.addGpsStatusListener(this);

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            start();
        //}

        /*sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        if (light != null) {
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
        }*/

        return super.onStartCommand(intent, flags, startId);
    }

    public void onProviderDisabled(String arg0) {
    }

    public void onProviderEnabled(String arg0) {
    }

    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    }

    public void onLocationChanged(Location location) {
        //if (location != null) {
        //}
        getSpeed(location);
        showNotification();
    }

    @SuppressLint("MissingPermission")
    public void getSatellitesCount() {
        GpsStatus mGpsStatus = lm.getGpsStatus(null);
        Iterable<GpsSatellite> satellites = mGpsStatus.getSatellites();
        int iTempCountInView = 0;
        int iTempCountInUse = 0;
        if (satellites != null) {
            for (GpsSatellite gpsSatellite : satellites) {
                iTempCountInView++;
                if (gpsSatellite.usedInFix()) {
                    iTempCountInUse++;
                }
            }
        }
        inView = iTempCountInView;      //Count not correct     sometimes less than 2-5
        inUse = iTempCountInUse;        //Count not correct     sometimes less than 2-5, same number as inView
    }

    private void getSpeed(Location location){
        double newTime= System.currentTimeMillis();
        double newLat = location.getLatitude();
        double newLon = location.getLongitude();
        if (location.hasSpeed()) {
            //Toast.makeText(this, "location.hasSpeed() true  lat: " + newLat + " long: " + newLon, Toast.LENGTH_SHORT).show();
            speed = location.getSpeed();
        }
        else {
            //Toast.makeText(this, "location.hasSpeed() false  lat: " + newLat + " long: " + newLon, Toast.LENGTH_SHORT).show();
            double distance = calculationBydistance(newLat,newLon,oldLat,oldLon);
            double timeDifferent = newTime - curTime;
            speed = distance/timeDifferent*1000;        //Convert m/milis to m/s
            curTime = newTime;
            oldLat = newLat;
            oldLon = newLon;
        }
    }

    private double calculationBydistance(double lat1, double lon1, double lat2, double lon2){
        double radius = 6371000;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return radius * c;
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

        //double valueA = (double)Math.round(aValue * 10) / 10;
        //double valuedV = (double)Math.round(dV * 3.6 * 10) / 10;
        //String contentText = "Accelerometer: "  + valueA + " m/s2" + "        Velocity: " + valueV + " km/h";

        //double valueV = (double)Math.round(speed * 3.6 * 10) / 10;

        if (speed > maxSpeed)
            maxSpeed = speed;

        if (gpsSatellites == true) {
            if (inUse > 0) {
                countSpeed += 1;
                totalSpeed += speed;
                avgSpeed = totalSpeed / (double) countSpeed;
                length += speed / (double) 3600;
            }
        }
        else {
            countSpeed += 1;
            totalSpeed += speed;
            avgSpeed = totalSpeed / (double) countSpeed;
            length += speed / (double) 3600;
        }

        double speedS = (double)Math.round(speed * 3.6 * 10) / 10;
        double totalSpeedS = (double)Math.round(totalSpeed * 3.6 * 10) / 10;
        double maxSpeedS = (double)Math.round(maxSpeed * 3.6 * 10) / 10;
        double avgSpeedS = (double)Math.round(avgSpeed * 3.6 * 10) / 10;
        double lengthS = (double)Math.round(length * 3.6 * 10) / 10;

        if (inUse > 0)
            strInUse = "In used";
        else strInUse = "Not used";
        if (inView > 0)
            strInView = "Connected";
        else strInView = "Not connected";

        double countS = countSpeed * ((double) requestInterval / 1000);

        String title = "";
        if (gpsSatellites == true)
            title = "(" + (double)Math.round(countS * 10) / 10 + "s) " + speedS + " km/h" + "        Satellites: " + strInUse + "/" + strInView;
        else
            title = speedS + " km/h" + " (" + (double)Math.round(countS * 10) / 10 + "s)";
            //title = speedS + " km/h" + " (" + countSpeed + "s) " + "        (debug)Total: " + totalSpeedS + " km/h";

        String contentText = "Max: " + maxSpeedS + " km/h    Avg: "  + avgSpeedS + " km/h    Length: " + lengthS + " km";

        Bitmap bitmap = createBitmapFromString(Double.toString(speedS), "km/h");
        Icon icon = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            icon = Icon.createWithBitmap(bitmap);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My notification a", "My notification a", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setVibrationPattern(new long[]{ 0 });
            channel.enableVibration(false);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder noti = new NotificationCompat.Builder(this, "My notification a")
                    //.setContentTitle("Internet Speed Meter" + "     " + connectionType)
                    .setContentTitle(title)
                    .setContentText(contentText)
                    //builder.setSmallIcon(R.mipmap.ic_launcher_round);
                    .setSmallIcon(IconCompat.createFromIcon(icon))
                    .setAutoCancel(false)
                    .setOnlyAlertOnce(true);

            //notificationManager.notify(3, noti.build());

            startForeground(3, noti.build());
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