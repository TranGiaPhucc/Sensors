package com.hufi.sensor;

import static androidx.core.app.ActivityCompat.startActivityForResult;

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
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechToText extends Service {

    String voiceDetected = "o";     //o: false     O: true

    private TextToSpeech t1;
    private SpeechRecognizer recognizer;

    public SpeechToText() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        t1.stop();
        t1.shutdown();
        recognizer.stopListening();
        recognizer.cancel();
        recognizer.destroy();
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.JAPAN);
                }
            }
        });

        start();

        return super.onStartCommand(intent, flags, startId);
    }

    private void start() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.domain.app");

        recognizer = SpeechRecognizer
                .createSpeechRecognizer(this);

        RecognitionListener listener = new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> voiceResults = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (voiceResults == null) {
                    System.out.println("No voice results");
                    voiceDetected = "null";
                } else {
                    System.out.println("Printing matches: ");
                    for (String match : voiceResults) {
                        System.out.println(match);
                        Toast.makeText(getApplicationContext(), match, Toast.LENGTH_LONG).show();
                        voiceDetected = "o";
                        t1.speak(match, TextToSpeech.QUEUE_FLUSH, null);

                        showNotification();

                        //recognizer.startListening(intent);
                        start();
                    }
                }
            }

            @Override
            public void onReadyForSpeech(Bundle params) {
                System.out.println("Ready for speech");
                voiceDetected = "R";
                showNotification();
            }

            /**
             *  ERROR_NETWORK_TIMEOUT = 1;
             *  ERROR_NETWORK = 2;
             *  ERROR_AUDIO = 3;
             *  ERROR_SERVER = 4;
             *  ERROR_CLIENT = 5;
             *  ERROR_SPEECH_TIMEOUT = 6;
             *  ERROR_NO_MATCH = 7;
             *  ERROR_RECOGNIZER_BUSY = 8;
             *  ERROR_INSUFFICIENT_PERMISSIONS = 9;
             *
             * @param error code is defined in SpeechRecognizer */

            @Override
            public void onError(int error) {
                System.err.println("Error listening for speech: " + error);
                voiceDetected = "err";
                showNotification();

                start();
            }

            @Override
            public void onBeginningOfSpeech() {
                System.out.println("Speech starting");
                voiceDetected = "S";
                Toast.makeText(getApplicationContext(), "Start recording voice", Toast.LENGTH_LONG).show();
                showNotification();
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onEndOfSpeech() {
                // TODO Auto-generated method stub
                voiceDetected = "E";
                showNotification();
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // TODO Auto-generated method stub

            }
        };

        recognizer.setRecognitionListener(listener);
        recognizer.startListening(intent);

        showNotification();
    }

    private void showNotification() {
        // TODO Auto-generated method stub

        String title = "Speech to text";
        String contentText = "";

        Bitmap bitmap = createBitmapFromString(voiceDetected, "o");
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