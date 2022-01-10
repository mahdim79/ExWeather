package com.dust.exweather.viewmodel.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasses.forecastweather.Forecastday
import com.dust.exweather.model.dataclasses.forecastweather.WeatherForecast
import com.dust.exweather.model.dataclasses.historyweather.WeatherHistory
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.model.room.CityDao
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.utils.DataStatus
import com.dust.exweather.utils.UtilityFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class CurrentFragmentViewModel(
    private val currentWeatherRepository: CurrentWeatherRepository,
    private val cityDao: CityDao,
    private val locationManager: LocationManager
) : ViewModel() {

    private val weatherLiveData = MutableLiveData<DataWrapper<MainWeatherData>>()

    fun getWeatherByCityName(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val cityNames = cityDao.getCitiesNames()
            if (!cityNames.isNullOrEmpty())
                getWeatherDataFromApi(cityNames[0].cityName, context)
        }
    }

    @SuppressLint("MissingPermission")
    fun getWeatherDataByUserLocation(context: Context) {
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (location != null)
                getWeatherDataFromApi("${location.latitude},${location.longitude}", context)
        } else {
            emitFailureState("لطفا موقعیت یاب دستگاه را روشن کنید")
        }
    }

    private fun getWeatherDataFromApi(q: String, context: Context) {
        emitLoadingState()
        if (!UtilityFunctions.isNetworkConnectionEnabled(context)) {
            emitFailureState("ارتباط با سرور میسر نیست")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val currentWeatherData: Response<CurrentData> =
                currentWeatherRepository.getCurrentWeatherData(q)
            var historyWeatherData: WeatherHistory? = null
            var forecastWeatherData: Response<WeatherForecast>? = null

            if (currentWeatherData.isSuccessful && currentWeatherData.body() != null) {
                val data = currentWeatherData.body()!!

                async {
                    for (i in 1 until 6)
                        launch {
                            val historyData = currentWeatherRepository.getHistoryWeatherData(
                                q,
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
                        forecastWeatherData = currentWeatherRepository.getForecastWeatherData(q)
                    }
                }.await()

                /*val persianTextResponse =
                    currentWeatherRepository.translateWord(data.current!!.condition.text)
                if (persianTextResponse.isSuccessful && persianTextResponse.body() != null && persianTextResponse.body()!!.ok)
                    data.current.condition.weatherPersianText = persianTextResponse.body()!!.result*/

                data.current!!.dayOfWeek =
                    UtilityFunctions.getDayOfWeekByUnixTimeStamp(data.location!!.localtime_epoch)

                val mainWeatherData = MainWeatherData(data, null, null)

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
                            mainWeatherData.current!!.current!!.dayOfWeek
                        )
                }

                currentWeatherRepository.insertWeatherDataToRoom(mainWeatherData)

                withContext(Dispatchers.Main) {
                    emitSuccessfulState(mainWeatherData)
                }
            } else {
                withContext(Dispatchers.Main) {
                    emitFailureState("خطایی رخ داده است!")
                }
            }
        }
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
                UtilityFunctions.getDayOfWeekByUnixTimeStamp(listDays[i].date_epoch)

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
                UtilityFunctions.getDayOfWeekByUnixTimeStamp(forecastDay.date_epoch)
        }

        return historyTempList
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

    fun calculateMainRecyclerViewDataList(data: MainWeatherData): List<DataWrapper<Any>> {
        val list = arrayListOf<DataWrapper<Any>>()
        data.forecastDetailsData!!.forecast.forecastday.forEach {
            list.add(DataWrapper(it.day, DataStatus.DATA_RECEIVE_SUCCESS))
        }

        list.reverse()

        list.add(DataWrapper(data.current!!, DataStatus.DATA_RECEIVE_SUCCESS))

        data.historyDetailsData!!.forecast.forecastday.forEach {
            list.add(DataWrapper(it.day, DataStatus.DATA_RECEIVE_SUCCESS))
        }

        return list
    }

    suspend fun getWeatherDataFromCache(): List<WeatherEntity> =
        currentWeatherRepository.getWeatherDataFromRoom()

    fun getWeatherLiveData(): MutableLiveData<DataWrapper<MainWeatherData>> =
        weatherLiveData

    private fun emitLoadingState() {
        weatherLiveData.value = DataWrapper(null, DataStatus.DATA_RECEIVE_LOADING)
    }

    private fun emitSuccessfulState(data: MainWeatherData) {
        weatherLiveData.value =
            DataWrapper(data = data, DataStatus.DATA_RECEIVE_SUCCESS)
    }

    private fun emitFailureState(e: String) {
        weatherLiveData.value =
            DataWrapper(
                MainWeatherData(CurrentData(null, null, e), null, null),
                DataStatus.DATA_RECEIVE_FAILURE
            )
    }
}