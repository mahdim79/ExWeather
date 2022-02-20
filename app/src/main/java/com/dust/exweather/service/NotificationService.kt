package com.dust.exweather.service

import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.annotation.UiThread
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.sharedpreferences.UnitManager
import com.dust.exweather.widget.WidgetUpdater
import dagger.android.DaggerService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import java.util.*
import javax.inject.Inject

class NotificationService : DaggerService() {

    @Inject
    lateinit var unitManager: UnitManager

    @Inject
    lateinit var widgetUpdater: WidgetUpdater

    private var timer: Timer? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (timer == null)
                startTimer()
        } catch (e: Exception) {
            stopSelf()
        }
        return START_STICKY
    }

    private fun startTimer() {
        timer = Timer()
        timer!!.schedule(NotificationTimerTask(), 0L, 1800000L)
    }

    private fun cancelTimer() {
        timer?.let {
            it.purge()
            it.cancel()
        }
    }

    @UiThread
    private fun configureDataAndNotification() {
        widgetUpdater.updateWidget()
    }

    override fun onDestroy() {
        cancelTimer()
        super.onDestroy()
    }

    inner class NotificationTimerTask : TimerTask() {
        override fun run() {
            configureDataAndNotification()
            Log.i("TimerWorker", "Loop")
        }

    }

}