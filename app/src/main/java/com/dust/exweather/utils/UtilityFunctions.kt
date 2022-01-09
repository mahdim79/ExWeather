package com.dust.exweather.utils

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.dust.exweather.model.dataclasses.currentweather.other.WeatherStatesDetails
import com.google.gson.Gson
import java.io.BufferedInputStream
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

class UtilityFunctions {
    companion object {
        fun isNetworkConnectionEnabled(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        }

        fun getWeatherStateGifUrl(weatherStatesDetails: WeatherStatesDetails , code:Int):String{
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
            return Gson().fromJson(stringBuilder.toString() , WeatherStatesDetails::class.java)
        }

        fun getFiveDaysLeft(currentData: Int, timeZone:String): String {
            val date = Date((currentData - 432000).toLong() * 1000)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            sdf.timeZone = TimeZone.getTimeZone(timeZone)
            return sdf.format(date)
        }
    }

}