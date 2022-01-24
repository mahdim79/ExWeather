package com.dust.exweather.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.dust.exweather.model.dataclasses.currentweather.other.WeatherStatesDetails
import com.google.gson.Gson
import java.io.BufferedInputStream
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

class UtilityFunctions {
    companion object {
        fun isNetworkConnectionEnabled(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        }

        fun getWeatherStateGifUrl(weatherStatesDetails: WeatherStatesDetails, code: Int): String {
            for (i in weatherStatesDetails)
                if (i.code == code)
                    return i.gifUrl
            return Constants.DEFAULT_WALLPAPER_URL
        }

        fun getWeatherStatesDetailsObject(context: Context): WeatherStatesDetails {
            val inputStream =
                context.applicationContext.resources.assets.open("json/weather_conditions.json")
            val bufferedInputStream = BufferedInputStream(inputStream)
            val stringBuilder = StringBuilder()
            while (bufferedInputStream.available() != -1)
                stringBuilder.append(bufferedInputStream.read().toChar())
            return Gson().fromJson(stringBuilder.toString(), WeatherStatesDetails::class.java)
        }

        fun getDaysLeft(currentTime: Int, timeZone: String, days:Int): String {
            val date = Date((currentTime - (days * 86400)).toLong() * 1000)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            sdf.timeZone = TimeZone.getTimeZone(timeZone)
            return sdf.format(date)
        }

        fun getDayOfWeekByUnixTimeStamp(unix: Int): String {
            val date = Date(unix.toLong() * 1000)
            val calendar = Calendar.getInstance()
            calendar.time = date
            return when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SATURDAY -> {
                    "شنبه"
                }
                Calendar.SUNDAY -> {
                    "یک شنبه"
                }
                Calendar.MONDAY -> {
                    "دوشنبه"
                }
                Calendar.TUESDAY -> {
                    "سه شنبه"
                }
                Calendar.WEDNESDAY -> {
                    "چهارشنبه"
                }
                Calendar.THURSDAY -> {
                    "پنج شنبه"
                }
                else -> {
                    "جمعه"
                }
            }
        }

        fun calculateLastUpdateText(systemLastUpdateEpoch: Long): String {
            val diff = System.currentTimeMillis() - systemLastUpdateEpoch
            return when {
                diff <= 60000 -> "به تازگی"
                diff in 60000..3600000 -> "${
                    String.format(
                        Locale.ENGLISH,
                        "%.0f",
                        floor((diff / 60000).toDouble())
                    )
                } دقیقه پیش"
                diff in 3600000..86400000 -> "${
                    String.format(
                        Locale.ENGLISH,
                        "%.0f",
                        floor((diff / 3600000).toDouble())
                    )
                } ساعت پیش"
                else -> "${
                    String.format(
                        Locale.ENGLISH,
                        "%.0f",
                        floor((diff / 86400000).toDouble())
                    )
                } روز پیش"
            }
        }

        fun calculateCurrentDateByTimeEpoch(timeEpoch:Int, tz:String = "Asia/tehran"):String{
            val date = Date((timeEpoch).toLong() * 1000)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            sdf.timeZone = TimeZone.getTimeZone(tz)
            return sdf.format(date)
        }

        fun calculateCurrentTimeByTimeEpoch(timeEpoch:Int, tz:String = "Asia/tehran"):String{
            val date = Date((timeEpoch).toLong() * 1000)
            val sdf = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            sdf.timeZone = TimeZone.getTimeZone(tz)
            return sdf.format(date)
        }

    }

}