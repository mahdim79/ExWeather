package com.dust.exweather.broadcast

import android.content.Context
import android.content.Intent
import com.dust.exweather.widget.WidgetUpdater
import dagger.android.DaggerBroadcastReceiver
import javax.inject.Inject

class WidgetUpdaterReceiver:DaggerBroadcastReceiver() {

    @Inject
    lateinit var widgetUpdater: WidgetUpdater

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        widgetUpdater.updateWidget()
    }
}