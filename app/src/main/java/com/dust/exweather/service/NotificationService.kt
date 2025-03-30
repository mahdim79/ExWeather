package com.dust.exweather.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.util.Log
import com.dust.exweather.broadcast.WidgetUpdaterReceiver

class NotificationService : JobService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.i("NotificationService","onStartJob")
        sendBroadcast(Intent(this,WidgetUpdaterReceiver::class.java))
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.i("NotificationService","onStopJob")
        return true
    }

}