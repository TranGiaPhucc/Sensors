package com.hufi.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AutoStart extends BroadcastReceiver {

    public void onReceive(Context context, Intent arg1)
    {
        /*Intent intent = new Intent(context, Sensor.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }

        Intent intent1 = new Intent(context, Sensor1.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent1);
        } else {
            context.startService(intent1);
        }*/

        /*Intent intent2 = new Intent(context, BatteryStatus.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent2);
        } else {
            context.startService(intent2);
        }*/

        Intent intent2 = new Intent(context, CpuStatus.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent2);
        } else {
            context.startService(intent2);
        }
    }
}