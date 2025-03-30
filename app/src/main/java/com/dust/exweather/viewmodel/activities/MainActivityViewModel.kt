package com.dust.exweather.viewmodel.activities

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.dust.exweather.MyApplication
import com.dust.exweather.service.NotificationService
import com.dust.exweather.utils.UtilityFunctions

class MainActivityViewModel : ViewModel() {

    fun getCurrentTheme(context: Context): Int {
        return (context.applicationContext as MyApplication).getCurrentThemeResId()
    }

    fun startNotificationService(context: Context) {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(5005)
        val cName = ComponentName(context.applicationContext, NotificationService::class.java)
        val info = JobInfo.Builder(5005, cName)
            .setPersisted(true)
            .setPeriodic(16 * 60 * 1000L)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .build()
        scheduler.schedule(info)

    }

}