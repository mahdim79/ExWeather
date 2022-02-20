package com.dust.exweather.service

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.annotation.UiThread
import com.dust.exweather.model.DataOptimizer
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
import com.dust.exweather.notification.NotificationManager
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.sharedpreferences.UnitManager
import com.dust.exweather.utils.Settings
import com.dust.exweather.utils.UtilityFunctions
import com.dust.exweather.widget.WidgetUpdater
import com.google.gson.Gson
import dagger.android.DaggerService
import kotlinx.coroutines.*
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class NotificationService : DaggerService() {

    @Inject
    lateinit var weatherDao: WeatherDao

    @Inject
    lateinit var mainApiRequests: MainApiRequests

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    @Inject
    lateinit var unitManager: UnitManager

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var widgetUpdater: WidgetUpdater

    @Inject
    lateinit var dataOptimizer: DataOptimizer

    private var timer: Timer? = null

    private var coroutineJob: Job? = null

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
        getWeatherDataFromApi(applicationContext)
    }

    private fun shouldSendNotification(): Boolean {
        val systemTimeEpoch = System.currentTimeMillis()
        var lastTimeEpoch = sharedPreferencesManager.getLastNotificationTimeEpoch()
        if (lastTimeEpoch == 0L)
            lastTimeEpoch = systemTimeEpoch
        val calendar = Calendar.getInstance()
        calendar.time = Date(systemTimeEpoch)
        return (systemTimeEpoch - lastTimeEpoch) > 86400000L && sharedPreferencesManager.getNotificationSettings() == Settings.NOTIFICATION_ON
    }

    private fun getWeatherDataFromApi(context: Context) {

        coroutineJob?.cancel(CancellationException("NormalCancellation"))
        coroutineJob = CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
            val cachedData = weatherDao.getDirectWeatherData()
            sharedPreferencesManager.getDefaultLocation()?.let { defLocation ->
                if (cachedData.isNullOrEmpty()) {
                    updateWidget(null, "")
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

                                        launch {
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
                                                        dataOptimizer.setUpHistoryFetchedData(
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
                                        }.join()
                                        data.current!!.day_of_week =
                                            UtilityFunctions.getDayOfWeekByUnixTimeStamp(
                                                data.location!!.localtime_epoch,
                                                applicationContext
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
                                                dataOptimizer.optimizeHistoryData(historyWeatherData!!.forecast.forecastday,applicationContext)
                                            mainWeatherData.historyDetailsData = historyWeatherData
                                        }

                                        if (forecastWeatherData!!.isSuccessful && forecastWeatherData!!.body() != null) {
                                            mainWeatherData.forecastDetailsData =
                                                forecastWeatherData!!.body()
                                            mainWeatherData.forecastDetailsData!!.forecast.forecastday =
                                                dataOptimizer.optimizeForecastData(
                                                    mainWeatherData.forecastDetailsData!!.forecast.forecastday,
                                                    mainWeatherData.current!!.current!!.day_of_week,
                                                    applicationContext
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
                                        updateWidget(
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
                                                applicationContext
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
                                        updateWidget(
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
            Log.i("ServiceJobJobStatus", "null")
            coroutineJob = null
        }
    }

    private fun updateWidget(mainWeatherData: MainWeatherData?, lastUpdate: String) {
        widgetUpdater.updateWidgetWithData(mainWeatherData, lastUpdate)
    }

    override fun onDestroy() {
        cancelTimer()
        coroutineJob?.cancel(CancellationException("NormalCancellation"))
        super.onDestroy()
    }

    inner class NotificationTimerTask : TimerTask() {
        override fun run() {
            configureDataAndNotification()
            Log.i("TimerWorker", "Loop")
        }

    }

}