package com.hufi.sensor;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.ByteArrayOutputStream;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpUriRequest;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.HttpClientBuilder;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class Sensor1 extends Service implements LocationListener, GpsStatus.Listener {
    public String dist = "";

    //private SensorManager sensorManager;
    //private Sensor light;
    //double vValue = 0;
    //double aValue = 0;
    //private double timestamp = 0;

    boolean gpsSatellites = false;
    boolean modeGPS = true;

    LocationManager lm;

    String weather = "";

    //GPS Satellites
    int inUse = 0;
    int inView = 0;
    String strInUse = "Not used";
    String strInView = "Not connected";

    double accuracy = 0;    //meter

    double speedCalc = 0;
    double speed = 0;
    double maxSpeed = 0;
    double maxCalcSpeed = 0;
    double avgCalcSpeed = 0;
    double deltaTime = 0;
    double time = 0;

    double time_0 = 0;
    double time_1 = 0;   //1-20
    double time_2 = 0;   //20-30
    double time_3 = 0;   //30-40
    double time_4 = 0;   //40-60
    double time_5 = 0;   //60+

    int timeWeatherPer100Secs = 0;

    double bearing = 0;
    double bearingPrev = 0;

    double totalSpeedCalc = 0;
    double lengthCalc = 0;
    double length = 0;
    double avgSpeed = 0;
    int countSpeed = 0;

    int lengthKm = 0;

    double curTime = 0;
    double oldLat = 0.0;
    double oldLon = 0.0;
    double newLat = 0.0;
    double newLon = 0.0;

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
        modeGPS = intent.getBooleanExtra("modeGPS", true);

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

        if (modeGPS)
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, this);
        else
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 0, this);

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
        if (location.hasSpeed()) {
            //Toast.makeText(this, "true  lat: " + newLat + " long: " + newLon, Toast.LENGTH_SHORT).show();
            speed = location.getSpeed();
            accuracy = location.getAccuracy();
        }
        else {
            //Toast.makeText(this, "false  lat: " + newLat + " long: " + newLon, Toast.LENGTH_SHORT).show();
            accuracy = location.getAccuracy();
            /*double distance = calculationBydistance(newLat,newLon,oldLat,oldLon);
            double timeDifferent = newTime - curTime;
            speedCalc = distance/timeDifferent*1000;        //Convert m/milis to m/s
            curTime = newTime;
            oldLat = newLat;
            oldLon = newLon;*/
        }

        //double newTime = System.currentTimeMillis();
        double newTime = Math.round(location.getElapsedRealtimeNanos() / 1000000);     //Convert nanos to milis
        newLat = location.getLatitude();
        newLon = location.getLongitude();

        if (!modeGPS) {
            int freqUpdate = (int) Math.round(time) / 100;
            if ((time == 0 || freqUpdate > timeWeatherPer100Secs) && CheckConnection.haveNetworkConnection(this)) {
                timeWeatherPer100Secs = freqUpdate;
                weather = getWeather(newLat, newLon);
            }
        }

        //{Test
        Location startPoint = new Location("locationA");
        startPoint.setLatitude(oldLat);
        startPoint.setLongitude(oldLon);

        Location endPoint = new Location("locationB");
        endPoint.setLatitude(newLat);
        endPoint.setLongitude(newLon);

        double distance = startPoint.distanceTo(endPoint);      //meter*/
        //}
        //double distance = calculationBydistance(newLat,newLon,oldLat,oldLon);     //meter

        if(location.hasBearing()) {
            bearing = location.getBearing();
        }
        bearingPrev = startPoint.bearingTo(endPoint);

        double timeDifferent = (newTime - curTime) / 1000;    //Convert milis to s

        if (oldLat != 0 || oldLon != 0) {
            speedCalc = distance / timeDifferent;
            deltaTime = timeDifferent;
            lengthCalc += distance;
            totalSpeedCalc += speedCalc;

            vibrateEveryKm(lengthCalc);
        }

        curTime = newTime;
        oldLat = newLat;
        oldLon = newLon;
    }

    private void vibrateEveryKm(double length) {
        int temp = (int) Math.floor(length / 1000);
        if (temp > lengthKm) {
            lengthKm = temp;

            //Toast.makeText(getApplicationContext(), temp + "/" + lengthKm + "/" + length, Toast.LENGTH_SHORT).show();

            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(1000);

            //long[] pattern = {500, 500, 500};
            //v.vibrate(pattern, 0);
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

        if (speedCalc > maxCalcSpeed)
            maxCalcSpeed = speedCalc;

        double speedS = (double)Math.round(speed * 3.6 * 10) / 10;
        double speedCalcS = (double)Math.round(speedCalc * 3.6 * 10) / 10;

        if (gpsSatellites == true) {
            if (inUse > 0) {
                countSpeed += 1;
                time += deltaTime;
                length += speed;        //length (meter)
                if (time > 0) {
                    avgSpeed = length / (double) countSpeed;
                    avgCalcSpeed = totalSpeedCalc / time;
                }
            }
        }
        else {
            countSpeed += 1;
            time += deltaTime;
            length += speed;        //length (meter)
            if (time > 0) {
                avgSpeed = length / (double) countSpeed;
                avgCalcSpeed = totalSpeedCalc / time;
            }

            if (speedS > 60)
                time_5 += deltaTime;
            else if (speedS > 40)
                time_4 += deltaTime;
            else if (speedS > 30)
                time_3 += deltaTime;
            else if (speedS > 20)
                time_2 += deltaTime;
            else if (speedS > 0)
                time_1 += deltaTime;
            else
                time_0 += deltaTime;
        }

        double accuracyS = (double)Math.round(accuracy * 10) / 10;
        double maxSpeedS = (double)Math.round(maxSpeed * 3.6 * 10) / 10;
        double avgSpeedS = (double)Math.round(avgSpeed * 3.6 * 10) / 10;

        double maxCalcSpeedS = (double)Math.round(maxCalcSpeed * 3.6 * 10) / 10;
        double avgCalcSpeedS = (double)Math.round(avgCalcSpeed * 3.6 * 10) / 10;
        double lengthCalcS = (double)Math.round(lengthCalc);

        double bearingS = (double)Math.round(bearing);
        double bearingPrevS = (double)Math.round(bearingPrev);

        double timeS = (double)Math.round(time * 10) / 10;
        double deltaTimeS = (double)Math.round(deltaTime * 10) / 10;

        if (inUse > 0)
            strInUse = "In used";
        else strInUse = "Not used";
        if (inView > 0)
            strInView = "Connected";
        else strInView = "Not connected";

        String district = "";

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(newLat, newLon, 1);
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            //Toast.makeText(this, "" + e, Toast.LENGTH_LONG).show();
        }

        if (addresses != null && addresses.size() > 0) {
            //district = addresses.get(0).getAddressLine(0);

            for (int i = 0; i < addresses.size(); i++)
                district += addresses.get(0).getAddressLine(i);
        }
        else district = "Không tìm thấy thông tin vị trí.";

        if (!district.equals(dist)) {
            dist = district;
            Toast.makeText(getApplicationContext(), dist, Toast.LENGTH_SHORT).show();

            DateFormat df = new SimpleDateFormat("dd/MM HH:mm:ss");
            String date = df.format(Calendar.getInstance().getTime());
            DiaChi d = new DiaChi(date + " (" + speedS + " km/h)", dist);

            Database db = new Database(Sensor1.this);        //getApplicationContext()
            db.insertDiaChi(d);
        }

        int h = (int) Math.floor(timeS / 3600);
        int m = (int) Math.floor((timeS - h * 3600) / 60);
        int s = (int) timeS - h * 3600 - m * 60;
        String hms = h + ":" + String.format("%02d", m) + ":" + String.format("%02d", s);

        /*int time_0_int = (int) time_0;
        int time_1_int = (int) time_1;
        int time_2_int = (int) time_2;
        int time_3_int = (int) time_3;
        int time_4_int = (int) time_4;
        int time_5_int = (int) time_5;*/


        String title = "";
        if (gpsSatellites == true)
            title = "(" + timeS + "s) " + speedS + " km/h        Satellites: " + strInUse + "/" + strInView;
        else
            title = speedS + " (" + speedCalcS +") km/h     Acc: " + accuracyS + " m     Time: " + hms + " (" + deltaTimeS + "s)";
            //title = speedS + " km/h" + " (" + countSpeed + "s) " + "        (debug)Total: " + totalSpeedS + " km/h";

        String content = "Max:          " + maxSpeedS + " (" + maxCalcSpeedS + ") km/h\nAverage:    "  + avgSpeedS + " (" + avgCalcSpeedS +") km/h\nLength:      " + (int) lengthCalcS + " m";
        String contentText = district;
        String expandText = "\n" + contentText + "\n\n" + "Bearing (from previous location): " + (int) bearingS + " (" + (int) bearingPrevS + ") degree\n\n" + content + "\n\n" + weather;

        String speedRecordText = "AFK: " + Math.round(time_0) + " s\n1-20 km/h: " + Math.round(time_1) + " s\n21-30 km/h: " + Math.round(time_2) + " s\n31-40 km/h: " + Math.round(time_3) + " s\n41-60 km/h: " + Math.round(time_4) + " s\n60+ km/h: " + Math.round(time_5) + " s";
        if (modeGPS)
            expandText += speedRecordText;

        //Send data to MainActivity.class
        Intent intent = new Intent("gps");
        // Adding some data
        /*intent.putExtra("accuracy", accuracyS);
        intent.putExtra("deltaTime", deltaTimeS);
        intent.putExtra("time", timeS);
        intent.putExtra("speed", speedS);
        intent.putExtra("speedCalc", speedCalcS);
        intent.putExtra("maxSpeed", maxSpeedS);
        intent.putExtra("maxSpeedCalc", maxCalcSpeedS);
        intent.putExtra("avgSpeed", avgSpeedS);
        intent.putExtra("avgSpeedCalc", avgCalcSpeedS);
        intent.putExtra("length", (int) lengthCalcS);
        intent.putExtra("district", district);*/
        intent.putExtra("gpsText", title + "\n" + expandText);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        //Send data to widget
        Intent intentWidget = new Intent(this, SpeedometerWidget.class);
        intentWidget.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        /*intentWidget.putExtra("accuracy", accuracyS);
        intentWidget.putExtra("deltaTime", deltaTimeS);
        intentWidget.putExtra("time", timeS);
        intentWidget.putExtra("speed", speedS);
        intentWidget.putExtra("speedCalc", speedCalcS);
        intentWidget.putExtra("maxSpeed", maxSpeedS);
        intentWidget.putExtra("maxSpeedCalc", maxCalcSpeedS);
        intentWidget.putExtra("avgSpeed", avgSpeedS);
        intentWidget.putExtra("avgSpeedCalc", avgCalcSpeedS);
        intentWidget.putExtra("length", (int) lengthCalcS);
        intentWidget.putExtra("district", district);*/
        intentWidget.putExtra("gpsText", title + "\n" + expandText);

        int[] ids = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, SpeedometerWidget.class));
        if(ids != null && ids.length > 0) {
            intentWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intentWidget);
        }

        //Notification
        Bitmap bitmap = createBitmapFromString(Integer.toString((int) speedS), Integer.toString((int) speedCalcS));
        Icon icon = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            icon = Icon.createWithBitmap(bitmap);
        }

        String gr_name = "Speedometer";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(gr_name, gr_name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setVibrationPattern(new long[]{ 0 });
            channel.enableVibration(false);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder noti = new NotificationCompat.Builder(this, gr_name)
                    //.setContentTitle("Internet Speed Meter" + "     " + connectionType)
                    .setContentTitle(title)
                    .setContentText(contentText)
                    //builder.setSmallIcon(R.mipmap.ic_launcher_round);
                    .setSmallIcon(IconCompat.createFromIcon(icon))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(expandText))
                    .setAutoCancel(false)
                    .setOnlyAlertOnce(true)
                    .setGroup(gr_name);

            //notificationManager.notify(3, noti.build());

            startForeground(3, noti.build());
        }
    }

    private Bitmap createBitmapFromString(String speed, String units) {
        //textsize: 55 + 45 = 95
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(55);
        paint.setTextAlign(Paint.Align.CENTER);

        Paint unitsPaint = new Paint();
        unitsPaint.setAntiAlias(true);
        unitsPaint.setTextSize(47); // size is in pixels
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

    private String getWeather(double lat, double lon) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected())
            return "";

        String api_key = "213ee2ea20ed0756ba8cf2498077e023";

        String urlWeather = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&lang=vi&APPID=" + api_key;
        String urlForecase = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&lang=vi&appid=" + api_key;

        String text = "";

        //Current weather
        String responseWeather = "";
        try {
            responseWeather = new HTTPReqTask().execute(urlWeather).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JSONObject js = null;
        try {
            String weatherDescription = "";

            js = new JSONObject(responseWeather);
            JSONArray weatherArray = js.optJSONArray("weather");

            for (int i=0; i < weatherArray.length(); i++){
                JSONObject w = weatherArray.optJSONObject(i);
                weatherDescription = w.optString("description");
            }

            double rain1h = 0;
            double rain3h = 0;
            if (js.optJSONObject("rain") != null) {
                rain1h = js.optJSONObject("rain").optDouble("1h");
                rain3h = js.optJSONObject("rain").optDouble("3h");
            }

            String placeName = js.optString("name");
            int clouds = js.optJSONObject("clouds").optInt("all");
            double temp = (double) Math.round((js.optJSONObject("main").optDouble("temp") - 273.15) * 10) / 10;      //Kelvin to C
            double feellike = (double) Math.round((js.optJSONObject("main").optDouble("feels_like") - 273.15) * 10) / 10;      //Kelvin to C
            int humid = js.optJSONObject("main").optInt("humidity");
            int pressure = js.optJSONObject("main").optInt("pressure");
            double wind = (double) Math.round(js.optJSONObject("wind").optDouble("speed") * 3.6 * 1000) / 1000;
            int wind_dir = js.optJSONObject("wind").optInt("deg");
            long sunrise = js.optJSONObject("sys").optLong("sunrise");
            long sunset = js.optJSONObject("sys").optLong("sunset");
            long datetime = js.optLong("dt");

            DateFormat df = new SimpleDateFormat("HH:mm:ss");
            String datetimeDate = df.format(new java.util.Date(datetime * 1000L));
            String sunriseDate = df.format(new java.util.Date(sunrise * 1000L));
            String sunsetDate = df.format(new java.util.Date(sunset * 1000L));

            text = "Vị trí: " + placeName + "\nThời tiết: " + weatherDescription + "\nMây: " + clouds + "%       Mưa 1h: " + rain1h + " mm       Mưa 3h: " + rain3h + " mm" +
                    "\nNhiệt độ: " + temp + "°C      Cảm giác như: " + feellike + "°C" +
                    "\n\nĐộ ẩm: " + humid + "%                  Áp suất: " + pressure + " hPa" + "\nTốc độ gió: " + wind + " km/h      Hướng gió: " + wind_dir + "°" +
                    "\n\nThời điểm cập nhật: " + datetimeDate + "\nMặt trời mọc: " + sunriseDate + "       Mặt trời lặn: " + sunsetDate;

        } catch (JSONException e) {
            e.printStackTrace();
            //Toast.makeText(this, "" + e, Toast.LENGTH_LONG).show();
        }

        //Forecase weather
        String responseForecase = "";
        try {
            responseForecase = new HTTPReqTask().execute(urlForecase).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        js = null;
        try {
            js = new JSONObject(responseForecase);

            text += "\n\nKhả năng có mưa: ";
            JSONArray listArray = js.optJSONArray("list");

            int arrayLength = 9;
            if (listArray.length() < arrayLength)
                arrayLength = listArray.length();

            for (int i=0; i < arrayLength; i++){
                JSONObject l = listArray.optJSONObject(i);

                long datetime = l.optLong("dt");
                double temp = (double) Math.round((l.optJSONObject("main").optDouble("temp") - 273.15) * 10) / 10;      //Kelvin to C

                String weatherDescription = "";
                JSONArray weatherArray = l.optJSONArray("weather");
                for (int j=0; j < weatherArray.length(); j++){
                    JSONObject w = weatherArray.optJSONObject(j);
                    weatherDescription = w.optString("description");
                }

                DateFormat df = new SimpleDateFormat("dd/MM HH:mm");
                String datetimeDate = df.format(new java.util.Date(datetime * 1000L));

                int pop = (int) Math.round(l.getDouble("pop") * 100);      //Probability of precipitation
                double rain3h = 0;
                if (l.optJSONObject("rain") != null)
                    rain3h = l.optJSONObject("rain").optDouble("3h");

                if (i == 0)
                    text += pop + "%\n";

                text += "\n     " + datetimeDate + "   " + temp + "°C  " + pop + "%    " + rain3h + " mm   " + weatherDescription;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            //Toast.makeText(this, "" + e, Toast.LENGTH_LONG).show();
        }

        //return
        return text;
    }

    private class HTTPReqTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = HttpClientBuilder.create().build();
            HttpUriRequest httpUriRequest = new HttpGet(params[0]);
            HttpResponse response = null;
            try {
                response = httpclient.execute(httpUriRequest);      //crash
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*URL url = null;
            try {
                url = new URL(params[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            String result = "";
            try {
                /*InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                result = IOUtils.toString(in);
                in.close();*/

                HttpEntity entity = response.getEntity();
                result = EntityUtils.toString(entity);

            } catch (IOException e) {
                e.printStackTrace();
            }
            /*finally {
                urlConnection.disconnect();
            }*/
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //Do anything with response..
        }
    }
}