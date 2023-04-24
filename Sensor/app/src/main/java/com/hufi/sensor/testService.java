package com.hufi.sensor;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Executor;


public class testService extends AccessibilityService {
    private ArrayList<String> mFoundWords = new ArrayList<String>();

    private String getEventText(AccessibilityEvent event)
    {
        StringBuilder sb = new StringBuilder();
        for (CharSequence s : event.getText())
        {
            sb.append(s);
        }
        return sb.toString();
    }

    public testService() {

    }

    @Override
    public void onServiceConnected() {
        Log.v("Connected :", "Onservice() Connected...");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 100;
        info.packageNames = null;
        setServiceInfo(info);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Settings.Secure.putString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "com.hufi.sensor/ccom.hufi.sensor.testService");
        Settings.Secure.putString(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED, "1");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void takeScreenshot(int displayId, @NonNull Executor executor, @NonNull TakeScreenshotCallback callback) {
        super.takeScreenshot(displayId, executor, callback);
    }
    
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo node = event.getSource();
        final String str = itterateThroughChildren(node);

        if(!str.equals(""))
        {
            Toast.makeText(testService.this, str, Toast.LENGTH_SHORT).show();
            if(mFoundWords.contains(str)) return;
            //Log.d(Common.TAG, "Found string: "+str);
            mFoundWords.add(str);
        }
/*
        takeScreenshot(Display.DEFAULT_DISPLAY,
                getApplicationContext().getMainExecutor(), new TakeScreenshotCallback() {
                    @Override
                    public void onSuccess(@NonNull ScreenshotResult screenshotResult) {

                        Log.i("ScreenShotResult","onSuccess");
                        Bitmap bitmap = Bitmap.wrapHardwareBuffer(screenshotResult.getHardwareBuffer(),screenshotResult.getColorSpace());
                        //AccessibilityUtils.saveImage(bitmap,getApplicationContext(),"WhatsappIntegration");

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
                                Toast.makeText(testService.this, "Scan Failed: Found nothing to scan", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(testService.this, blocks, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(testService.this, "Could not set up the detector!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int i) {

                        Log.i("ScreenShotResult","onFailure code is "+ i);

                    }
                });
        */
    }

    private String itterateThroughChildren(AccessibilityNodeInfo node)
    {
        if(node != null)
        {
            if(node.getClassName().equals("android.widget.EditText") && node.getPackageName().equals("com.google.android.googlequicksearchbox")) return node.getText().toString();
            for(int i=0;i<node.getChildCount();i++)
            {
                AccessibilityNodeInfo childNodeView = node.getChild(i);
                if(childNodeView != null)
                {
                    if(childNodeView.getClassName().equals("android.widget.EditText") && childNodeView.getPackageName().equals("com.google.android.googlequicksearchbox")) return childNodeView.getText().toString();
                    String test = itterateThroughChildren(childNodeView);
                    if(!test.equals("")) return test;
                }
            }
        }
        return "";
    }

    @Override
    public void onInterrupt() {
        Log.d("Interrupt", "onInterrupt() is Called...");
    }
}