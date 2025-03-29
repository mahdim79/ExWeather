package com.dust.exweather.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import com.dust.exweather.model.DataOptimizer
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasses.forecastweather.WeatherForecast
import com.dust.exweather.model.dataclasses.historyweather.WeatherHistory
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.dataclasses.weatherwidget.WidgetData
import com.dust.exweather.model.retrofit.MainApiRequests
import com.dust.exweather.model.room.WeatherDao
import com.dust.exweather.model.toDataClass
import com.dust.exweather.model.toEntity
import com.dust.exweather.notification.NotificationManager
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.sharedpreferences.UnitManager
import com.dust.exweather.utils.Settings
import com.dust.exweather.utils.UtilityFunctions
import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.Response
import java.util.*

class WidgetUpdater(
    private val context: Context,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val weatherDao: WeatherDao,
    private val mainApiRequests: MainApiRequests,
    private val unitManager: UnitManager,
    private val dataOptimizer: DataOptimizer,
    private val notificationManager: NotificationManager
) {

    private var coroutineJob: Job? = null

    fun updateWidget() {
        Log.i("WidgetUpdater","updating widget...")
        coroutineJob?.cancel(CancellationException("NormalCancellation"))
        coroutineJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val cachedData = weatherDao.getDirectWeatherData()
                sharedPreferencesManager.getDefaultLocation()?.let { defLocation ->
                    if (cachedData.isNullOrEmpty()) {
                        updateWidgetWithData(null, "")
                    } else {
                        cachedData.forEach { entity ->
                            if (entity.toDataClass().location == defLocation) {

                                if (UtilityFunctions.isNetworkConnectionEnabled(context)) {
                                    var newData: MainWeatherData? = null
                                    launch {
                                        val weatherData = entity.toDataClass()
                                        val currentWeatherData: Response<CurrentData>? = try {
                                            mainApiRequests.getCurrentWeatherData(weatherData.location)
                                        }catch (e:Exception){
                                            null
                                        }
                                        var historyWeatherData: WeatherHistory? = null
                                        var forecastWeatherData: Response<WeatherForecast>? = null

                                        if (currentWeatherData?.body() != null && currentWeatherData.isSuccessful) {
                                            val data = currentWeatherData.body()!!

                                            launch {
                                                for (i in 1 until 6)
                                                    launch {
                                                        val historyData = try {
                                                            mainApiRequests.getHistoryWeatherData(
                                                                weatherData.location,
                                                                UtilityFunctions.getDaysLeft(
                                                                    data.location!!.localtime_epoch,
                                                                    data.location.tz_id,
                                                                    i
                                                                )
                                                            )
                                                        }catch (e:Exception){
                                                            null
                                                        }

                                                        val optimizedData =
                                                            dataOptimizer.setUpHistoryFetchedData(
                                                                historyWeatherData,
                                                                historyData
                                                            )
                                                        if (optimizedData != null)
                                                            historyWeatherData = optimizedData

                                                    }
                                                launch {
                                                    forecastWeatherData = try {
                                                        mainApiRequests.getForecastWeatherData(
                                                            weatherData.location
                                                        )
                                                    }catch (e:Exception){
                                                        null
                                                    }

                                                }
                                            }.join()
                                            data.current!!.day_of_week =
                                                UtilityFunctions.getDayOfWeekByUnixTimeStamp(
                                                    data.location!!.localtime_epoch,
                                                    context
                                                )

                                            val mainWeatherData = MainWeatherData(
                                                data,
                                                null,
                                                null,
                                                weatherData.location,
                                                weatherData.id
                                            )

                                            // sort historical data
                                            if (historyWeatherData != null) {
                                                historyWeatherData!!.forecast.forecastday =
                                                    dataOptimizer.optimizeHistoryData(
                                                        historyWeatherData!!.forecast.forecastday,
                                                        context
                                                    )
                                                mainWeatherData.historyDetailsData = historyWeatherData
                                            }

                                            if (forecastWeatherData!!.isSuccessful && forecastWeatherData!!.body() != null) {
                                                mainWeatherData.forecastDetailsData =
                                                    forecastWeatherData!!.body()
                                                mainWeatherData.forecastDetailsData!!.forecast.forecastday =
                                                    dataOptimizer.optimizeForecastData(
                                                        mainWeatherData.forecastDetailsData!!.forecast.forecastday,
                                                        mainWeatherData.current!!.current!!.day_of_week,
                                                        context
                                                    )
                                            }

                                            mainWeatherData.current!!.current!!.system_last_update_epoch =
                                                System.currentTimeMillis()

                                            newData = mainWeatherData
                                        }
                                    }.join()

                                    newData?.let { newWeatherData ->
                                        weatherDao.insertWeatherData(arrayListOf(newWeatherData.toEntity()))

                                        withContext(Dispatchers.Main) {
                                            // update widget
                                            val calendar = Calendar.getInstance()
                                            updateWidgetWithData(
                                                newWeatherData,
                                                "${
                                                    String.format(
                                                        Locale.ENGLISH,
                                                        "%02d",
                                                        calendar.get(Calendar.HOUR_OF_DAY)
                                                    )
                                                }:${
                                                    String.format(
                                                        Locale.ENGLISH,
                                                        "%02d",
                                                        calendar.get(Calendar.MINUTE)
                                                    )
                                                }"
                                            )

                                            // send notification
                                            if (shouldSendNotification())
                                                notificationManager.sendNotification(
                                                    newWeatherData,
                                                    context
                                                )

                                        }
                                    }

                                } else {
                                    withContext(Dispatchers.Main) {
                                        val offlineData = entity.toDataClass()
                                        val calendar = Calendar.getInstance()
                                        offlineData.current?.current?.system_last_update_epoch?.let {
                                            calendar.time = Date(it)
                                            updateWidgetWithData(
                                                offlineData,
                                                "${
                                                    String.format(
                                                        Locale.ENGLISH,
                                                        "%02d",
                                                        calendar.get(Calendar.HOUR_OF_DAY)
                                                    )
                                                }:${
                                                    String.format(
                                                        Locale.ENGLISH,
                                                        "%02d",
                                                        calendar.get(Calendar.MINUTE)
                                                    )
                                                }"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                coroutineJob = null
            }catch (e:Exception){}
        }
    }

    private fun shouldSendNotification(): Boolean {
        val systemTimeEpoch = System.currentTimeMillis()
        val lastTimeEpoch = sharedPreferencesManager.getLastNotificationTimeEpoch()
        val calendar = Calendar.getInstance()
        calendar.time = Date(systemTimeEpoch)
        return ((systemTimeEpoch - lastTimeEpoch) > 86400000L) && (sharedPreferencesManager.getNotificationSettings() == Settings.NOTIFICATION_ON) && (calendar.get(
            Calendar.HOUR_OF_DAY
        ) == 12)
    }

    private fun updateWidgetWithData(mainWeatherData: MainWeatherData?, lastUpdate: String) {
        Intent("android.appwidget.action.APPWIDGET_UPDATE").apply {
            putExtra(
                "WeatherData",
                Gson().toJson(
                    WidgetData(
                        mainWeatherData?.current?.location?.name ?: "null",
                        mainWeatherData?.current?.current?.condition?.text ?: "null",
                        unitManager.getPrecipitationUnit(
                            mainWeatherData?.current?.current?.precip_mm?.toString() ?: "null",
                            mainWeatherData?.current?.current?.precip_in?.toString() ?: "null"
                        ),
                        unitManager.getTemperatureUnit(
                            mainWeatherData?.current?.current?.temp_c?.toString() ?: "null",
                            mainWeatherData?.current?.current?.temp_f?.toString() ?: "null"
                        ),
                        lastUpdate,
                        ((mainWeatherData?.current?.current?.is_day ?: 0) == 1),
                        mainWeatherData?.current?.current?.condition?.icon
                    )
                )
            )
            context.sendBroadcast(this)
        }
    }

}