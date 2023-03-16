package com.hufi.sensor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.ToneGenerator;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.tts.TextToSpeech;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    CheckBox cbxLight, cbxAcc, cbxGravity, cbxInternet, cbxSatellites, cbxSpeechToText, cbxScreenTranslateOCR, cbxGPSMode;
    Button btnSpeech, btnScreenshot;
    TextToSpeech t1;
    ImageView imgScreenshot;
    TextView txtOCR, lbGPS;

    boolean gpsSatellites = false;
    boolean modeGPS = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(messageReceiverOCR, new IntentFilter("ocr"));

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(messageReceiverGPS, new IntentFilter("gps"));


        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.JAPAN);
                }
            }
        });

        cbxGPSMode = findViewById(R.id.cbxGPSMode);
        cbxLight = findViewById(R.id.cbxLight);
        cbxAcc = findViewById(R.id.cbxAcc);
        cbxGravity = findViewById(R.id.cbxGravity);
        cbxInternet = findViewById(R.id.cbxInternet);
        cbxSatellites = findViewById(R.id.cbxSatellites);
        cbxSpeechToText = findViewById(R.id.cbxSpeechToText);
        btnSpeech = findViewById(R.id.btnSpeech);
        cbxScreenTranslateOCR = findViewById(R.id.cbxScreenTranslateOCR);
        btnScreenshot = findViewById(R.id.btnScreenshot);
        imgScreenshot = findViewById(R.id.imgScreenshot);
        txtOCR = findViewById(R.id.txtOCR);
        lbGPS = findViewById(R.id.lbGPS);

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

        if (!isMyServiceRunning(ScreenTranslateOCR.class))
        {
            cbxScreenTranslateOCR.setChecked(false);
        }
        else
            cbxScreenTranslateOCR.setChecked(true);

        btnSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String toSpeak = "Ohaiyo";
                Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        btnScreenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //takeScreenshot();
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 1);
            }
        });

        imgScreenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgScreenshot.setDrawingCacheEnabled(true);
                takeScreenshot(imgScreenshot.getDrawingCache());
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

        cbxGPSMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    modeGPS = true;
                else
                    modeGPS = false;
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
                                intent.putExtra("modeGPS", modeGPS);
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

        cbxScreenTranslateOCR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            requestPermissions(permissions,1);

                            cbxScreenTranslateOCR.setChecked(false);
                        }
                        else if (!isMyServiceRunning(ScreenTranslateOCR.class)) {
                            Intent intent = new Intent(MainActivity.this, ScreenTranslateOCR.class);
                            startService(intent);
                        }
                    }
                }
                else {
                    stopService(new Intent(MainActivity.this, ScreenTranslateOCR.class));
                }
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

    private void takeScreenshot(Bitmap bitmap) {
        //Date now = new Date();
        //DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            imgScreenshot.setImageBitmap(null);
            txtOCR.setText("");

            // create bitmap screen capture (in app)
            /*View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);*/

            TextRecognizer detector = new TextRecognizer.Builder(getApplicationContext()).build();
            if (detector.isOperational() && bitmap != null) {
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> textBlocks = detector.detect(frame);
                String blocks = "";
                for (int index = 0; index < textBlocks.size(); index++) {
                    TextBlock tBlock = textBlocks.valueAt(index);
                    blocks = blocks + tBlock.getValue();
                    if (index < textBlocks.size() - 1)
                        blocks = blocks + "\n";
                }
                if (textBlocks.size() == 0) {
                    Toast.makeText(this, "Scan Failed: Found nothing to scan", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, blocks, Toast.LENGTH_LONG).show();
                    txtOCR.setText(blocks);
                }
            } else {
                Toast.makeText(this, "Could not set up the detector!", Toast.LENGTH_SHORT).show();
            }

            imgScreenshot.setImageBitmap(bitmap);

        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    private BroadcastReceiver messageReceiverOCR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            //takeScreenshot();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            Bitmap bmp = null;
            try {
                //Upload
                bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
                takeScreenshot(bmp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private BroadcastReceiver messageReceiverGPS = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            double accuracy = intent.getDoubleExtra("accuracy", 0);
            int time = intent.getIntExtra("time", 0);
            double speed = intent.getDoubleExtra("speed", 0);
            double speedCalc = intent.getDoubleExtra("speedCalc", 0);
            double maxSpeed = intent.getDoubleExtra("maxSpeed", 0);
            double maxSpeedCalc = intent.getDoubleExtra("maxSpeedCalc", 0);
            double avgSpeed = intent.getDoubleExtra("avgSpeed", 0);
            double avgSpeedCalc = intent.getDoubleExtra("avgSpeedCalc", 0);
            int length = intent.getIntExtra("length", 0);
            String district = intent.getStringExtra("district");

            String contentText = "    " + speed + " (" + speedCalc + ") " + "km/h (" + time + " s)        Accuracy: " + accuracy + " m" +
                    "\n\nMax:          " + maxSpeed + " (" + maxSpeedCalc + ") km/h\nAverage:   " + avgSpeed + " (" + avgSpeedCalc + ") km/h\nLength:     " + length + " m" +
                    "\n\n" + district;

            lbGPS.setText(contentText);
        }
    };
}