package com.dust.exweather.viewmodel.fragments

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dust.exweather.R
import com.dust.exweather.model.DataOptimizer
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasses.forecastweather.Forecastday
import com.dust.exweather.model.dataclasses.forecastweather.WeatherForecast
import com.dust.exweather.model.dataclasses.historyweather.WeatherHistory
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import com.dust.exweather.utils.DataStatus
import com.dust.exweather.utils.UtilityFunctions
import com.dust.exweather.utils.convertAmPm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class ForecastDetailsFragmentViewModel(
    private val currentWeatherRepository: CurrentWeatherRepository,
    private val dataOptimizer: DataOptimizer
):ViewModel() {
    private val weatherApiCallStateLiveData = MutableLiveData<DataWrapper<String>>()

    fun getWeatherDataFromApi(context: Context) {
        emitLoadingState()

        if (!UtilityFunctions.isNetworkConnectionEnabled(context)) {
            emitFailureState(context.getString(R.string.connectionLost))
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val cachedData = currentWeatherRepository.getDirectWeatherDataFromCache()
            val listNewData = arrayListOf<MainWeatherData>()

            launch {
                cachedData.forEach {
                    launch {
                        val weatherData = it.toDataClass()
                        val currentWeatherData: Response<CurrentData> =
                            currentWeatherRepository.getCurrentWeatherData(weatherData.location)
                        var historyWeatherData: WeatherHistory? = null
                        var forecastWeatherData: Response<WeatherForecast>? = null

                        if (currentWeatherData.isSuccessful && currentWeatherData.body() != null) {
                            val data = currentWeatherData.body()!!

                            launch {
                                for (i in 1 until 6)
                                    launch {
                                        val historyData =
                                            currentWeatherRepository.getHistoryWeatherData(
                                                weatherData.location,
                                                UtilityFunctions.getDaysLeft(
                                                    data.location!!.localtime_epoch,
                                                    data.location.tz_id,
                                                    i
                                                )
                                            )
                                        val optimizedData =
                                            dataOptimizer.setUpHistoryFetchedData(historyWeatherData, historyData)
                                        if (optimizedData != null)
                                            historyWeatherData = optimizedData

                                    }
                                launch {
                                    forecastWeatherData =
                                        currentWeatherRepository.getForecastWeatherData(weatherData.location)
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
                                mainWeatherData.forecastDetailsData = forecastWeatherData!!.body()
                                mainWeatherData.forecastDetailsData!!.forecast.forecastday =
                                    dataOptimizer.optimizeForecastData(
                                        mainWeatherData.forecastDetailsData!!.forecast.forecastday,
                                        mainWeatherData.current!!.current!!.day_of_week,
                                        context
                                    )
                            }

                            mainWeatherData.current!!.current!!.system_last_update_epoch =
                                System.currentTimeMillis()

                            listNewData.add(mainWeatherData)
                        }
                    }
                }

            }.join()

            if (!listNewData.isNullOrEmpty()) {
                currentWeatherRepository.insertWeatherDataToRoom(listNewData)

                withContext(Dispatchers.Main) {
                    emitSuccessfulState()
                }
            }
        }
    }

    fun getLiveWeatherDataFromCache(): LiveData<List<WeatherEntity>> =
        currentWeatherRepository.getLiveWeatherDataFromCache()

    private fun emitLoadingState() {
        weatherApiCallStateLiveData.value = DataWrapper(null, DataStatus.DATA_RECEIVE_LOADING)
    }

    private fun emitSuccessfulState() {
        weatherApiCallStateLiveData.value = DataWrapper(null, DataStatus.DATA_RECEIVE_SUCCESS)
    }

    private fun emitFailureState(e: String) {
        weatherApiCallStateLiveData.value =
            DataWrapper(
                e,
                DataStatus.DATA_RECEIVE_FAILURE
            )
    }

    fun getWeatherApiCallStateLiveData(): LiveData<DataWrapper<String>> =
        weatherApiCallStateLiveData

    fun calculateForecastWeatherDetailsData(context: Context, forecastDay: Forecastday):String{
        val stringBuilder = StringBuilder()
        context.apply {
            stringBuilder.apply {
                append(
                    getString(
                        R.string.sunrise,
                        forecastDay.astro.sunrise.convertAmPm(context)
                    ).plus("\n")
                )
                append(
                    getString(
                        R.string.sunset,
                        forecastDay.astro.sunset.convertAmPm(context)
                    ).plus("\n")
                )
                append(
                    getString(
                        R.string.moonrise,
                        forecastDay.astro.moonrise.convertAmPm(context)
                    ).plus("\n")
                )
                append(
                    getString(
                        R.string.moonset,
                        forecastDay.astro.moonset.convertAmPm(context)
                    ).plus("\n")
                )
                append(
                    getString(
                        R.string.moonIllumination,
                        forecastDay.astro.moon_illumination.convertAmPm(context)
                    ).plus("\n")
                )
                append(
                    getString(R.string.moonPhase, forecastDay.astro.moon_phase.convertAmPm(context)).plus(
                        "\n"
                    )
                )
                append(getString(R.string.uvIndex, forecastDay.day.uv.toString()))
            }
        }
        return stringBuilder.toString()
    }

    fun calculateCurrentData(listData: List<MainWeatherData>, location:String?, date:String?): MainWeatherData? {
        if (!location.isNullOrEmpty() && !date.isNullOrEmpty()) {
            listData.forEach { mainWeatherData ->
                if (mainWeatherData.location == location)
                    return mainWeatherData
            }
        }
        return null
    }

    fun calculateData(data: MainWeatherData, date:String?): Forecastday? {
        data.forecastDetailsData?.let { weatherForecast ->
            weatherForecast.forecast.forecastday.forEach { forecastDay ->
                if (forecastDay.date == date)
                    return forecastDay
            }
        }
        return null
    }
}