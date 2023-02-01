package com.hufi.sensor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    CheckBox cbxLight, cbxAcc, cbxGravity, cbxInternet, cbxSatellites;
    boolean gpsSatellites = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cbxLight = findViewById(R.id.cbxLight);
        cbxAcc = findViewById(R.id.cbxAcc);
        cbxGravity = findViewById(R.id.cbxGravity);
        cbxInternet = findViewById(R.id.cbxInternet);
        cbxSatellites = findViewById(R.id.cbxSatellites);

        if (!isMyServiceRunning(Sensor.class))
        {
            cbxLight.setChecked(false);
        }
        else
            cbxLight.setChecked(true);

        if (!isMyServiceRunning(Sensor1.class))
        {
            cbxAcc.setChecked(false);
        }
        else {
            cbxAcc.setChecked(true);
            cbxSatellites.setEnabled(false);
        }

        if (!isMyServiceRunning(Sensor2.class))
        {
            cbxGravity.setChecked(false);
        }
        else
            cbxGravity.setChecked(true);

        if (!isMyServiceRunning(InternetSpeedMeter.class))
        {
            cbxInternet.setChecked(false);
        }
        else
            cbxInternet.setChecked(true);

        cbxSatellites.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    gpsSatellites = true;
                else
                    gpsSatellites = false;
            }
        });

        cbxLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (!isMyServiceRunning(Sensor.class))
                        startService(new Intent(MainActivity.this, Sensor.class));
                }
                else
                    stopService(new Intent(MainActivity.this, Sensor.class));
            }
        });

        cbxAcc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
                            requestPermissions(permissions,1);
                            String[] permissions1 = {Manifest.permission.ACCESS_COARSE_LOCATION};
                            requestPermissions(permissions1,2);

                            cbxAcc.setChecked(false);
                            cbxSatellites.setEnabled(true);
                        }
                        else {
                            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                Toast.makeText(MainActivity.this, "GPS disabled.", Toast.LENGTH_LONG).show();

                                cbxAcc.setChecked(false);
                                cbxSatellites.setEnabled(true);
                            }
                            else if (!isMyServiceRunning(Sensor1.class)) {
                                Intent intent = new Intent(MainActivity.this, Sensor1.class);
                                intent.putExtra("gpsSatellites", gpsSatellites);
                                startService(intent);

                                cbxSatellites.setEnabled(false);
                            }
                        }
                    }
                }
                else {
                    stopService(new Intent(MainActivity.this, Sensor1.class));
                    cbxSatellites.setEnabled(true);
                }
            }
        });

        cbxGravity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (!isMyServiceRunning(Sensor2.class))
                        startService(new Intent(MainActivity.this, Sensor2.class));
                }
                else
                    stopService(new Intent(MainActivity.this, Sensor2.class));
            }
        });

        cbxInternet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (!isMyServiceRunning(InternetSpeedMeter.class))
                        startService(new Intent(MainActivity.this, InternetSpeedMeter.class));
                }
                else
                    stopService(new Intent(MainActivity.this, InternetSpeedMeter.class));
            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}