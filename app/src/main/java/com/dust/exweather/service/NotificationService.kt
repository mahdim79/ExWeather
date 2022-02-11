package com.dust.exweather.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.UiThread
import androidx.core.app.NotificationCompat
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasses.forecastweather.Forecastday
import com.dust.exweather.model.dataclasses.forecastweather.WeatherForecast
import com.dust.exweather.model.dataclasses.historyweather.WeatherHistory
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.retrofit.MainApiRequests
import com.dust.exweather.model.room.WeatherDao
import com.dust.exweather.model.toDataClass
import com.dust.exweather.model.toEntity
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.ui.activities.SplashActivity
import com.dust.exweather.utils.Settings
import com.dust.exweather.utils.UtilityFunctions
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

    private var timer: Timer? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (timer == null)
                startTimer()
        } catch (e: Exception) {
            Log.i("TimerWorker", "catch Exception!")
            stopSelf()
        }
        return START_STICKY
    }

    private fun startTimer() {
        timer = Timer()
        timer!!.schedule(NotificationTimerTask(), 0L, 15000L)
    }

    private fun cancelTimer() {
        Log.i("TimerWorker", "timer canceled!")
        timer?.let {
            it.purge()
            it.cancel()
        }
    }

    @UiThread
    private fun configureDataAndNotification() {
        val systemTimeEpoch = System.currentTimeMillis()
        var lastTimeEpoch = sharedPreferencesManager.getLastNotificationTimeEpoch()
        if (lastTimeEpoch == 0L)
            lastTimeEpoch = systemTimeEpoch
        val calendar = Calendar.getInstance()
        calendar.time = Date(systemTimeEpoch)
        if (calendar.get(Calendar.HOUR_OF_DAY) == 12 || (systemTimeEpoch - lastTimeEpoch) > 86400000L) {
            getWeatherDataFromApi(applicationContext)
        }
    }

    private fun getWeatherDataFromApi(context: Context) {

        if (!UtilityFunctions.isNetworkConnectionEnabled(context)) {
            sharedPreferencesManager.setLastNotificationTimeEpoch(0L)
            return
        }

        Log.i("notificationServiceProcess", "stage1 done!")

        CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
            val cachedData = weatherDao.getDirectWeatherData()
            val listNewData = arrayListOf<MainWeatherData>()

            Log.i("notificationServiceProcess", "stage2 done!")

            async {
                cachedData.forEach {
                    launch {
                        val weatherData = it.toDataClass()
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
                                            setUpHistoryFetchedData(historyWeatherData, historyData)
                                        if (optimizedData != null)
                                            historyWeatherData = optimizedData

                                    }
                                launch {
                                    forecastWeatherData =
                                        mainApiRequests.getForecastWeatherData(weatherData.location)
                                }
                            }.await()
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
                                    optimizeHistoryData(historyWeatherData!!.forecast.forecastday)
                                mainWeatherData.historyDetailsData = historyWeatherData
                            }

                            if (forecastWeatherData!!.isSuccessful && forecastWeatherData!!.body() != null) {
                                mainWeatherData.forecastDetailsData = forecastWeatherData!!.body()
                                mainWeatherData.forecastDetailsData!!.forecast.forecastday =
                                    optimizeForecastData(
                                        mainWeatherData.forecastDetailsData!!.forecast.forecastday,
                                        mainWeatherData.current!!.current!!.day_of_week
                                    )
                            }

                            mainWeatherData.current!!.current!!.system_last_update_epoch =
                                System.currentTimeMillis()

                            listNewData.add(mainWeatherData)
                        }
                    }
                }

            }.await()

            Log.i("notificationServiceProcess", "stage3 done!")


            if (!listNewData.isNullOrEmpty()) {
                weatherDao.insertWeatherData(listNewData.map { it.toEntity() })

                withContext(Dispatchers.Main) {
                    Log.i("notificationServiceProcess", "stage4 done!")

                    sendNotification(listNewData[0])
                }
            }
        }
    }

    private fun sendNotification(data: MainWeatherData) {
        if (sharedPreferencesManager.getNotificationSettings() == Settings.NOTIFICATION_OFF)
            return

        val contentIntent = Intent(this, SplashActivity::class.java)
        contentIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        sharedPreferencesManager.setLastNotificationTimeEpoch(System.currentTimeMillis())
        val notification = NotificationCompat.Builder(applicationContext, "someId")
            .setCustomContentView(RemoteViews(packageName, R.layout.notification_normal_layout))
            .setCustomBigContentView(RemoteViews(packageName, R.layout.notification_big_layout))
            .setSmallIcon(R.drawable.ic_cloud)
            .setContentTitle("ExWeather")
            .setOngoing(false)
            .setContentIntent(
                PendingIntent.getActivity(
                    applicationContext,
                    201,
                    contentIntent,
                    0
                )
            )
            .setVibrate(LongArray(3) { 200L })
            .build()

        data.current?.let { current ->
            current.current?.let { currentData ->

                notification.contentView.apply {

                    current.location?.let { locaiton ->
                        setTextViewText(R.id.locationTextView, locaiton.name)
                    }

                    setTextViewText(
                        R.id.temperatureText,
                        applicationContext.resources.getString(
                            R.string.temperatureText,
                            currentData.temp_c.toString()
                        )
                    )

                    setTextViewText(
                        R.id.precipitationText,
                        applicationContext.resources.getString(
                            R.string.precipitationText,
                            currentData.precip_mm.toString()
                        )
                    )

                    setTextViewText(R.id.weatherStateText, currentData.condition.text)

                }

                notification.bigContentView.apply {
                    setTextViewText(
                        R.id.temperatureTextView,
                        applicationContext.resources.getString(
                            R.string.temperatureText,
                            currentData.temp_c.toString()
                        )
                    )

                    setTextViewText(
                        R.id.windSpeedTextView,
                        (applicationContext.resources.getString(
                            R.string.windSpeedText,
                            currentData.wind_kph.toString()
                        ).plus(" ").plus(currentData.wind_dir)
                                )
                    )

                    setTextViewText(
                        R.id.weatherStateText,
                        currentData.condition.text
                    )

                    current.location?.let { location ->
                        setTextViewText(
                            R.id.localTimeTextView,
                            UtilityFunctions.calculateCurrentTimeByTimeEpoch(
                                location.localtime_epoch,
                                location.tz_id
                            )
                        )

                        setTextViewText(
                            R.id.localDateTextView,
                            currentData.day_of_week.plus(" ").plus(
                                UtilityFunctions.calculateCurrentDateByTimeEpoch(
                                    location.localtime_epoch,
                                    location.tz_id
                                )
                            )
                        )

                        setTextViewText(R.id.locationNameTextView, location.name)
                    }

                    setTextViewText(
                        R.id.airPressureText,
                        applicationContext.resources.getString(
                            R.string.airPressureText,
                            currentData.pressure_mb.toString()
                        )
                    )

                    setTextViewText(
                        R.id.precipitationText,
                        applicationContext.resources.getString(
                            R.string.precipitationText,
                            currentData.precip_mm.toString()
                        )
                    )
                    setTextViewText(
                        R.id.humidityText,
                        applicationContext.resources.getString(
                            R.string.humidityText,
                            currentData.humidity.toString()
                        )
                    )

                }

                val notificationManager =
                    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(101, notification)
            }
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
                    applicationContext
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
                    applicationContext
                )
        }

        return historyTempList
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