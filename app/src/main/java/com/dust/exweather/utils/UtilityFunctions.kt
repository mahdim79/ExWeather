package com.dust.exweather.utils

import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.currentweather.main.Condition
import com.dust.exweather.model.dataclasses.currentweather.main.Location
import com.dust.exweather.service.NotificationService
import dagger.android.support.DaggerAppCompatActivity
import saman.zamani.persiandate.PersianDate
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

class UtilityFunctions {
    companion object {

        fun getConditionText(conditionText:String,conditionCode:Int,context: Context,isDay:Boolean = true):String{
            if (Locale.getDefault().language != "fa")
                return conditionText
            if (conditionCode == 1000){
                return if (isDay)
                    context.getString(R.string.Sunny)
                else
                    context.getString(R.string.Clear)
            }else{
                try {
                    val stringName = "conditionTextCode${conditionCode}"
                    val stringId = context.resources.getIdentifier(stringName,"string",context.packageName)
                    return context.getString(stringId)
                }catch (e:Exception){
                    return conditionText
                }
            }
        }

        fun isNetworkConnectionEnabled(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        }

        fun getDaysLeft(currentTime: Int, timeZone: String, days: Int): String {
            val date = Date((currentTime - (days * 86400)).toLong() * 1000)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            sdf.timeZone = TimeZone.getTimeZone(timeZone)
            return sdf.format(date)
        }

        fun getDayOfWeekByUnixTimeStamp(unix: Int, context: Context): String {
            val date = Date(unix.toLong() * 1000)
            val calendar = Calendar.getInstance()
            calendar.time = date
            return when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SATURDAY -> context.getString(R.string.saturday)
                Calendar.SUNDAY -> context.getString(R.string.sunday)
                Calendar.MONDAY -> context.getString(R.string.monday)
                Calendar.TUESDAY -> context.getString(R.string.tuesday)
                Calendar.WEDNESDAY -> context.getString(R.string.wednesday)
                Calendar.THURSDAY -> context.getString(R.string.thursday)
                else -> context.getString(R.string.friday)
            }
        }

        fun calculateLastUpdateText(systemLastUpdateEpoch: Long, context: Context): String {
            val diff = System.currentTimeMillis() - systemLastUpdateEpoch
            return when {
                diff <= 60000 -> context.getString(R.string.recently)
                diff in 60000..3600000 -> {
                    context.getString(
                        R.string.minuteAgo, String.format(
                            Locale.ENGLISH,
                            "%.0f",
                            floor((diff / 60000).toDouble())
                        )
                    )
                }
                diff in 3600000..86400000 -> {
                    context.getString(
                        R.string.hourAgo, String.format(
                            Locale.ENGLISH,
                            "%.0f",
                            floor((diff / 3600000).toDouble())
                        )
                    )
                }
                else -> {
                    context.getString(
                        R.string.dayAgo, String.format(
                            Locale.ENGLISH,
                            "%.0f",
                            floor((diff / 86400000).toDouble())
                        )
                    )
                }
            }
        }

        fun calculateCurrentDateByTimeEpoch(timeEpoch: Int, tz: String = "Asia/tehran"): String {
            val persianDate = PersianDate((timeEpoch).toLong() * 1000)
            return "${persianDate.shYear}-${persianDate.shMonth}-${persianDate.shDay}"
        }

        fun calculateCurrentTimeByTimeEpoch(timeEpoch: Int, tz: String = "Asia/tehran"): String {
            val date = Date((timeEpoch).toLong() * 1000)
            val sdf = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            sdf.timeZone = TimeZone.getTimeZone(tz)
            return sdf.format(date)
        }

        fun createLatLongPattern(locationObj: Location): String {
            return "${locationObj.lat},${locationObj.lon}"
        }

        fun calculateDayOfMonth(dateEpoch: Int): Array<Int> {
            val calendar = Calendar.getInstance()
            calendar.time = Date(dateEpoch.toLong() * 1000)
            return arrayOf(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun checkNotificationServiceRunning(context: Context): Boolean {
            val activityManager =
                context.getSystemService(DaggerAppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
            activityManager.getRunningServices(Int.MAX_VALUE).forEach {
                if (it.service.className == NotificationService::class.java.name)
                    return true
            }
            return false
        }

        fun getWeatherIconResId(icon: String?, isDay: Int?, context: Context): Int? {

            var resId: Int? = null
            icon?.let {
                val weatherCode = icon.substring(icon.lastIndexOf("/") + 1 , icon.lastIndexOf("."))
                isDay?.let {
                    resId = if (isDay == 1) {
                        context.resources.getIdentifier(
                            "w$weatherCode",
                            "drawable",
                            context.packageName
                        )
                    } else {
                        context.resources.getIdentifier(
                            "n$weatherCode",
                            "drawable",
                            context.packageName
                        )
                    }

                }
            }
            if (resId != 0)
                return resId
            return null
        }

    }

}