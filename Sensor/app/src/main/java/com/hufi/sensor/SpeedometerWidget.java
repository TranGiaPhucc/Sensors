package com.hufi.sensor;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * Implementation of App Widget functionality.
 */
public class SpeedometerWidget extends AppWidgetProvider {
    String text = "";

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        //CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object

        LocalBroadcastManager.getInstance(context)
                .registerReceiver(messageReceiverGPS, new IntentFilter("gps"));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.speedometer_widget);
        views.setTextViewText(R.id.appwidget_text, text);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

/*    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Bundle extras = intent.getExtras();
        if(extras!=null) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), SpeedometerWidget.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }*/

    private BroadcastReceiver messageReceiverGPS = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            double accuracy = intent.getDoubleExtra("accuracy", 0);
            double time = intent.getDoubleExtra("time", 0);
            double deltaTime = intent.getDoubleExtra("deltaTime", 0);
            double speed = intent.getDoubleExtra("speed", 0);
            double speedCalc = intent.getDoubleExtra("speedCalc", 0);
            double maxSpeed = intent.getDoubleExtra("maxSpeed", 0);
            double maxSpeedCalc = intent.getDoubleExtra("maxSpeedCalc", 0);
            double avgSpeed = intent.getDoubleExtra("avgSpeed", 0);
            double avgSpeedCalc = intent.getDoubleExtra("avgSpeedCalc", 0);
            int length = intent.getIntExtra("length", 0);
            String district = intent.getStringExtra("district");

            String contentText = speed + " (" + speedCalc + ") km/h (" + time + "s)     Acc: " + accuracy + " m     Freq: " + deltaTime + " s" +
                    "\n\nMax:          " + maxSpeed + " (" + maxSpeedCalc + ") km/h\nAverage:   " + avgSpeed + " (" + avgSpeedCalc + ") km/h\nLength:     " + length + " m" +
                    "\n\n" + district;

            text = contentText;

            Bundle extras = intent.getExtras();
            if(extras!=null) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName thisAppWidget = new ComponentName(context.getPackageName(), SpeedometerWidget.class.getName());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }
    };
}