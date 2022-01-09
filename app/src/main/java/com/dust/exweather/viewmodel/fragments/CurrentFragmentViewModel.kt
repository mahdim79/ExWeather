package com.dust.exweather.viewmodel.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
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
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

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

    suspend fun calculateMainRecyclerViewItems() {
        viewModelScope.launch(Dispatchers.IO) {

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
            var currentWeatherData: Response<CurrentData> =
                currentWeatherRepository.getCurrentWeatherData(q)
            var historyWeatherData: Response<WeatherHistory>? = null
            var forecastWeatherData: Response<WeatherForecast>? = null

            if (currentWeatherData.isSuccessful && currentWeatherData.body() != null) {
                val data = currentWeatherData.body()!!

                async {
                    launch {
                        historyWeatherData = currentWeatherRepository.getHistoryWeatherData(
                            q,
                            UtilityFunctions.getFiveDaysLeft(data.location!!.localtime_epoch, data.location.tz_id)
                        )
                    }
                    launch {
                        forecastWeatherData = currentWeatherRepository.getForecastWeatherData(q)
                    }
                }.await()

                val persianTextResponse =
                    currentWeatherRepository.translateWord(data.current!!.condition.text)
                if (persianTextResponse.isSuccessful && persianTextResponse.body() != null && persianTextResponse.body()!!.ok)
                    data.current.condition.weatherPersianText = persianTextResponse.body()!!.result

                val mainWeatherData = MainWeatherData(data, null, null)

                if (historyWeatherData!!.isSuccessful && historyWeatherData!!.body() != null)
                    mainWeatherData.historyData = historyWeatherData!!.body()

                if (forecastWeatherData!!.isSuccessful && forecastWeatherData!!.body() != null)
                    mainWeatherData.forecastData = forecastWeatherData!!.body()

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