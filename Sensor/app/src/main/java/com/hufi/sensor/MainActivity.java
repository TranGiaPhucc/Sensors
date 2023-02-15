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
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    CheckBox cbxLight, cbxAcc, cbxGravity, cbxInternet, cbxSatellites, cbxSpeechToText;
    Button btnSpeech;
    TextToSpeech t1;

    boolean gpsSatellites = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.JAPAN);
                }
            }
        });

        cbxLight = findViewById(R.id.cbxLight);
        cbxAcc = findViewById(R.id.cbxAcc);
        cbxGravity = findViewById(R.id.cbxGravity);
        cbxInternet = findViewById(R.id.cbxInternet);
        cbxSatellites = findViewById(R.id.cbxSatellites);
        cbxSpeechToText = findViewById(R.id.cbxSpeechToText);
        btnSpeech = findViewById(R.id.btnSpeech);

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

        if (!isMyServiceRunning(SpeechToText.class))
        {
            cbxSpeechToText.setChecked(false);
        }
        else
            cbxSpeechToText.setChecked(true);

        btnSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String toSpeak = "Ohaiyo";
                Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        cbxSatellites.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    gpsSatellites = true;
                else
                    gpsSatellites = false;
            }
        });

        cbxSpeechToText.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            String[] permissions = {Manifest.permission.RECORD_AUDIO};
                            requestPermissions(permissions, 1);

                            cbxSpeechToText.setChecked(false);
                        }
                        else if (!isMyServiceRunning(SpeechToText.class)) {
                            Intent intent = new Intent(MainActivity.this, SpeechToText.class);
                            //intent.putExtra("speech", speech);
                            startService(intent);
                        }
                    }
                }
                else
                    stopService(new Intent(MainActivity.this, SpeechToText.class));
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
                            //cbxSatellites.setEnabled(true);
                            //cbxSatellites.setVisibility(View.VISIBLE);
                        }
                        else {
                            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                Toast.makeText(MainActivity.this, "GPS disabled.", Toast.LENGTH_LONG).show();

                                cbxAcc.setChecked(false);
                                //cbxSatellites.setEnabled(true);
                                //cbxSatellites.setVisibility(View.VISIBLE);
                            }
                            else if (!isMyServiceRunning(Sensor1.class)) {
                                Intent intent = new Intent(MainActivity.this, Sensor1.class);
                                intent.putExtra("gpsSatellites", gpsSatellites);
                                startService(intent);

                                //cbxSatellites.setEnabled(false);
                                //cbxSatellites.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                }
                else {
                    stopService(new Intent(MainActivity.this, Sensor1.class));
                    //cbxSatellites.setEnabled(true);
                    //cbxSatellites.setVisibility(View.VISIBLE);
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