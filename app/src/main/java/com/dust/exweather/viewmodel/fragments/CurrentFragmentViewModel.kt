package com.dust.exweather.viewmodel.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.model.room.CityDao
import com.dust.exweather.model.room.CurrentWeatherEntity
import com.dust.exweather.utils.DataStatus
import com.dust.exweather.utils.UtilityFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CurrentFragmentViewModel(
    private val currentWeatherRepository: CurrentWeatherRepository,
    private val cityDao: CityDao,
    private val locationManager: LocationManager
) : ViewModel() {

    private val currentWeatherDataMutableLiveData = MutableLiveData<DataWrapper<CurrentData>>()

    fun getCurrentWeatherByCityName(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val cityNames = cityDao.getCitiesNames()
            if (!cityNames.isNullOrEmpty())
                getCurrentWeatherDataFromApi(cityNames[0].cityName , context)
        }
    }

    @SuppressLint("MissingPermission")
    fun getCurrentWeatherDataByUserLocation(context: Context) {
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (location != null)
                getCurrentWeatherDataFromApi("${location.latitude},${location.longitude}" , context)
        } else {
            emitFailureState("لطفا موقعیت یاب دستگاه را روشن کنید")
        }
    }

    private fun getCurrentWeatherDataFromApi(q: String , context:Context) {
        emitLoadingState()
        if (!UtilityFunctions.isNetworkConnectionEnabled(context)) {
            emitFailureState("ارتباط با سرور میسر نیست")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val mainResponse = currentWeatherRepository.getCurrentWeatherData(q)
            if (mainResponse.isSuccessful && mainResponse.body() != null) {
                val data = mainResponse.body()!!
                val persianTextResponse =
                    currentWeatherRepository.translateWord(data.current!!.condition.text)
                if (persianTextResponse.isSuccessful && persianTextResponse.body() != null && persianTextResponse.body()!!.ok)
                    data.current.condition.weatherPersianText = persianTextResponse.body()!!.result
                currentWeatherRepository.insertCurrentWeatherDataToRoom(data)
                withContext(Dispatchers.Main) {
                    Log.i("Data_fetched", data.toString())
                    emitSuccessfulState(data)
                }
            } else {
                withContext(Dispatchers.Main) {
                    emitFailureState("خطایی رخ داده است!")
                }
            }
        }
    }

    fun getCurrentWeatherDataFromCache(): LiveData<List<CurrentWeatherEntity>> =
        currentWeatherRepository.getCurrentWeatherDataFromRoom()

    fun getCurrentWeatherLiveData(): MutableLiveData<DataWrapper<CurrentData>> =
        currentWeatherDataMutableLiveData

    private fun emitLoadingState() {
        currentWeatherDataMutableLiveData.value = DataWrapper(null, DataStatus.DATA_RECEIVE_LOADING)
    }

    private fun emitSuccessfulState(data: CurrentData) {
        currentWeatherDataMutableLiveData.value =
            DataWrapper(data = data, DataStatus.DATA_RECEIVE_SUCCESS)
    }

    private fun emitFailureState(e: String) {
        currentWeatherDataMutableLiveData.value =
            DataWrapper(CurrentData(null, null, e), DataStatus.DATA_RECEIVE_FAILURE)
    }
}