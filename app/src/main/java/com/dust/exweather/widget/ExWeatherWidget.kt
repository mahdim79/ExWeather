package com.dust.exweather.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.weatherwidget.WidgetData
import com.dust.exweather.service.NotificationService
import com.dust.exweather.ui.activities.SplashActivity
import com.dust.exweather.utils.UtilityFunctions
import com.google.gson.Gson
import java.util.*

/**
 * Implementation of App Widget functionality.
 */
class ExWeatherWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        try {
            context.stopService(Intent(context, NotificationService::class.java))
        } catch (e: Exception) {
        }
        context.startService(Intent(context, NotificationService::class.java))
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        context?.let {
            intent?.extras?.getString("WeatherData")?.let { str ->
                updateCustomWidget(context, Gson().fromJson(str, WidgetData::class.java))
            }
        }
    }
}

internal fun updateCustomWidget(context: Context, data: WidgetData) {
    val widgetText = context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetIds =
        appWidgetManager.getAppWidgetIds(ComponentName(context, ExWeatherWidget::class.java))

    if (data.location == "null") {
        val views = RemoteViews(context.packageName, R.layout.ex_weather_widget_no_data)

        val intent = Intent(context, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        views.setOnClickPendingIntent(
            R.id.remoteViewContainer,
            PendingIntent.getActivity(context, 200, intent, 0)
        )

        // Instruct the widget manager to update the widget
        for (i in appWidgetIds)
            appWidgetManager.updateAppWidget(i, views)

    } else {
        val views = RemoteViews(context.packageName, R.layout.ex_weather_widget)
        views.setTextViewText(R.id.appwidget_text, widgetText)
        views.setTextViewText(R.id.weatherStateText, data.weatherState)
        views.setTextViewText(R.id.weatherCityNameText, data.location)
        views.setTextViewText(R.id.precipText, data.precipitation)
        views.setTextViewText(R.id.lastUpdateText, data.lastUpdate)
        views.setTextViewText(R.id.weatherTempText, data.temp)

        val iconResId = UtilityFunctions.getWeatherIconResId(
            data.icon,
            if (data.isDay) 1 else 0,
            context
        )

        views.setImageViewResource(R.id.cloudImage, iconResId ?: R.drawable.ic_launcher)
        views.setImageViewResource(R.id.cityImage, R.drawable.ic_country)
        views.setImageViewResource(R.id.precipImage, R.drawable.ic_rain)
        views.setImageViewResource(R.id.lastUpdateImage, R.drawable.ic_last_update)

        if (data.isDay)
            views.setImageViewResource(R.id.backgroundImage, R.drawable.day_bg)
        else
            views.setImageViewResource(R.id.backgroundImage, R.drawable.night_bg)

        val intent = Intent(context, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        views.setOnClickPendingIntent(
            R.id.remoteViewContainer,
            PendingIntent.getActivity(context, 200, intent, 0)
        )

        // Instruct the widget manager to update the widget
        for (i in appWidgetIds)
            appWidgetManager.updateAppWidget(i, views)
    }

}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val widgetText = context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.ex_weather_widget)
    views.setTextViewText(R.id.appwidget_text, widgetText)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}