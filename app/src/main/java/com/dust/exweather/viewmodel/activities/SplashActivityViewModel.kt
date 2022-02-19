package com.dust.exweather.viewmodel.activities

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.dust.exweather.MyApplication
import com.dust.exweather.service.NotificationService
import com.dust.exweather.utils.UtilityFunctions
import java.util.*

class SplashActivityViewModel:ViewModel() {

    fun getCurrentTheme(context: Context):Int {
        return (context.applicationContext as MyApplication).getCurrentThemeResId()
    }

    fun startNotificationService(context: Context) {
        if (!UtilityFunctions.checkNotificationServiceRunning(context))
            context.startService(Intent(context, NotificationService::class.java))
    }
}