package com.hufi.sensor;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * Implementation of App Widget functionality.
 */
public class SpeedometerWidget extends AppWidgetProvider {
    String text = "";
    String image = "";

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        //CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.speedometer_widget);
        views.setTextViewText(R.id.speedometerappwidget_text, text);

        Bitmap bmp = null;
        if (image != "" && image != null) {
            byte[] bytes = Base64.decode(image, Base64.DEFAULT);
            bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

        if (bmp != null)
            views.setImageViewBitmap(R.id.widget_image, bmp);

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

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        text = intent.getStringExtra("gpsText");
        image = intent.getStringExtra("webView");

        Bundle extras = intent.getExtras();
        if(extras!=null) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), SpeedometerWidget.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }
}