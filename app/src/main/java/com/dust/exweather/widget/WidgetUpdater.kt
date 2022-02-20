package com.dust.exweather.widget

import android.content.Context
import android.content.Intent
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasses.forecastweather.Forecastday
import com.dust.exweather.model.dataclasses.forecastweather.WeatherForecast
import com.dust.exweather.model.dataclasses.historyweather.WeatherHistory
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.dataclasses.weatherwidget.WidgetData
import com.dust.exweather.model.retrofit.MainApiRequests
import com.dust.exweather.model.room.WeatherDao
import com.dust.exweather.model.toDataClass
import com.dust.exweather.model.toEntity
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.sharedpreferences.UnitManager
import com.dust.exweather.utils.UtilityFunctions
import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.Response
import java.util.*

class WidgetUpdater(
    private val weatherDao: WeatherDao,
    private val mainApiRequests: MainApiRequests,
    private val context: Context,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val unitManager: UnitManager
) {

    private var coroutineJob: Job? = null


    fun updateWidget() {
        coroutineJob?.cancel(CancellationException("NormalCancellation"))
        coroutineJob = CoroutineScope(Dispatchers.IO).launch {
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
                                    val currentWeatherData: Response<CurrentData> =
                                        mainApiRequests.getCurrentWeatherData(weatherData.location)
                                    var historyWeatherData: WeatherHistory? = null
                                    var forecastWeatherData: Response<WeatherForecast>? = null

                                    if (currentWeatherData.isSuccessful && currentWeatherData.body() != null) {
                                        val data = currentWeatherData.body()!!

                                        async {
                                            for (i in 1 until 6)
                                                launch {
                                                    val historyData =
                                                        mainApiRequests.getHistoryWeatherData(
                                                            weatherData.location,
                                                            UtilityFunctions.getDaysLeft(
                                                                data.location!!.localtime_epoch,
                                                                data.location.tz_id,
                                                                i
                                                            )
                                                        )
                                                    val optimizedData =
                                                        setUpHistoryFetchedData(
                                                            historyWeatherData,
                                                            historyData
                                                        )
                                                    if (optimizedData != null)
                                                        historyWeatherData = optimizedData

                                                }
                                            launch {
                                                forecastWeatherData =
                                                    mainApiRequests.getForecastWeatherData(
                                                        weatherData.location
                                                    )
                                            }
                                        }.await()
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
                                                optimizeHistoryData(historyWeatherData!!.forecast.forecastday)
                                            mainWeatherData.historyDetailsData = historyWeatherData
                                        }

                                        if (forecastWeatherData!!.isSuccessful && forecastWeatherData!!.body() != null) {
                                            mainWeatherData.forecastDetailsData =
                                                forecastWeatherData!!.body()
                                            mainWeatherData.forecastDetailsData!!.forecast.forecastday =
                                                optimizeForecastData(
                                                    mainWeatherData.forecastDetailsData!!.forecast.forecastday,
                                                    mainWeatherData.current!!.current!!.day_of_week
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

                                    }
                                }

                            } else {
                                withContext(Dispatchers.Main) {
                                    sharedPreferencesManager.setLastNotificationTimeEpoch(0L)
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
        }
    }

    fun updateWidgetWithData(mainWeatherData: MainWeatherData?, lastUpdate: String) {
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
                        ((mainWeatherData?.current?.current?.is_day ?: 0) == 1)
                    )
                )
            )
            context.sendBroadcast(this)
        }
    }

    @Synchronized
    private fun setUpHistoryFetchedData(
        staticData: WeatherHistory?,
        response: Response<WeatherHistory>?
    ): WeatherHistory? {

        if (response!!.isSuccessful && response.body() != null) {
            val tempData = response.body()
            return if (staticData == null) {
                tempData
            } else {
                val tempArray =
                    arrayListOf<com.dust.exweather.model.dataclasses.historyweather.Forecastday>()
                tempArray.addAll(staticData.forecast.forecastday)
                tempArray.addAll(tempData!!.forecast.forecastday)
                staticData.forecast.forecastday = tempArray
                staticData
            }
        }
        return null
    }

    private fun optimizeForecastData(
        forecastday: List<Forecastday>,
        currentDayOfWeek: String
    ): List<Forecastday> {
        val listDays = arrayListOf<Forecastday>()
        listDays.addAll(forecastday)

        // calculate day of week
        for (i in listDays.indices)
            listDays[i].day.dayOfWeek =
                UtilityFunctions.getDayOfWeekByUnixTimeStamp(
                    listDays[i].date_epoch,
                    context
                )

        // find duplicate data and delete it from list
        if (!listDays.isNullOrEmpty())
            if (listDays[0].day.dayOfWeek == currentDayOfWeek)
                listDays.removeAt(0)
        return listDays
    }

    private fun optimizeHistoryData(
        forecastday: List<com.dust.exweather.model.dataclasses.historyweather.Forecastday>
    ): List<com.dust.exweather.model.dataclasses.historyweather.Forecastday> {

        // sort data by time epoch because its fetched by parallel method
        val historyTempList =
            arrayListOf<com.dust.exweather.model.dataclasses.historyweather.Forecastday>()
        historyTempList.addAll(forecastday)
        historyTempList.sortWith { p0, p1 ->
            if (p0.date_epoch > p1.date_epoch)
                -1
            else
                1
        }

        // calculate day of week
        for (i in historyTempList.indices) {
            val forecastDay =
                historyTempList[i]
            historyTempList[i].day.dayOfWeek =
                UtilityFunctions.getDayOfWeekByUnixTimeStamp(
                    forecastDay.date_epoch,
                    context
                )
        }

        return historyTempList
    }


}