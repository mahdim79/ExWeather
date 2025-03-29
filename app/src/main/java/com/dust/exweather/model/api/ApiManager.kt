package com.dust.exweather.model.api

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dust.exweather.R
import com.dust.exweather.model.DataOptimizer
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasses.forecastweather.WeatherForecast
import com.dust.exweather.model.dataclasses.historyweather.WeatherHistory
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.model.toDataClass
import com.dust.exweather.utils.DataStatus
import com.dust.exweather.utils.UtilityFunctions
import kotlinx.coroutines.*
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiManager @Inject constructor() {

    private val weatherApiCallStateLiveData = MutableLiveData<DataWrapper<String>>()

    @Inject
    lateinit var dataOptimizer:DataOptimizer

    private var mainCoroutineJob:Job? = null

    fun getWeatherDataFromApi(context: Context,currentWeatherRepository:CurrentWeatherRepository) {
        emitLoadingState()

        if (!UtilityFunctions.isNetworkConnectionEnabled(context)) {
            emitFailureState(context.getString(R.string.connectionLost))
            return
        }
        mainCoroutineJob?.cancel(CancellationException("Normal Coroutine Cancellation"))
        mainCoroutineJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val cachedData = currentWeatherRepository.getDirectWeatherDataFromCache()
                val updatedDataList = arrayListOf<MainWeatherData>()

                launch {
                    cachedData.forEach {
                        launch {
                            val weatherData = it.toDataClass()
                            val currentWeatherData: Response<CurrentData>? =
                                currentWeatherRepository.getCurrentWeatherData(weatherData.location)
                            var historyWeatherData: WeatherHistory? = null
                            var forecastWeatherData: Response<WeatherForecast>? = null

                            if (currentWeatherData?.body() != null && currentWeatherData.isSuccessful) {
                                val data = currentWeatherData.body()!!

                                // send parallel requests to api
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

                                if (forecastWeatherData?.body() != null && forecastWeatherData!!.isSuccessful) {
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

                                updatedDataList.add(mainWeatherData)
                            }else{
                                weatherApiCallStateLiveData.postValue(DataWrapper(null,DataStatus.DATA_RECEIVE_FAILURE))
                            }
                        }
                    }

                }.join()

                if (!updatedDataList.isNullOrEmpty()) {
                    currentWeatherRepository.insertWeatherDataToRoom(updatedDataList)

                    withContext(Dispatchers.Main) {
                        emitSuccessfulState()
                    }
                }
                mainCoroutineJob = null
            }catch (e:Exception){
                weatherApiCallStateLiveData.postValue(DataWrapper(null,DataStatus.DATA_RECEIVE_FAILURE))
            }
        }
    }

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

}