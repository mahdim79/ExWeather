package com.dust.exweather.viewmodel.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.model.room.CityDao
import com.dust.exweather.model.room.CurrentWeatherEntity
import com.dust.exweather.utils.DataStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CurrentFragmentViewModel(
    private val currentWeatherRepository: CurrentWeatherRepository,
    private val cityDao: CityDao,
    private val locationManager: LocationManager
) : ViewModel() {

    private val currentWeatherDataMutableLiveData = MutableLiveData<DataWrapper<CurrentData>>()

    private val dataObserver = Observer<DataWrapper<CurrentData>> {
        currentWeatherDataMutableLiveData.value = it
    }

    private var currentWeatherLiveData: LiveData<DataWrapper<CurrentData>>? = null

    fun getCurrentWeatherByCityName() {
        viewModelScope.launch(Dispatchers.IO) {
            var cityName = "London"
            try {
                cityName = cityDao.getCitiesNames()[0].cityName
            } catch (e: Exception) {
                Log.i("FetchCityNameException", "city name cache load exception")
            }
            withContext(Dispatchers.Main) {
                setUpObserver(getCurrentWeatherData(query = cityName))
            }
        }
    }

    private fun getCurrentWeatherData(query: String): LiveData<DataWrapper<CurrentData>> {
        return currentWeatherRepository.getCurrentWeatherData(query = query)
    }

    fun getCurrentWeatherDataFromCache(): LiveData<List<CurrentWeatherEntity>> =
        currentWeatherRepository.getCurrentWeatherDataFromRoom()

    @SuppressLint("MissingPermission")
    fun getCurrentWeatherDataByUserLocation() {
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            getWeatherDataByLocation(location)
        } else {
            currentWeatherDataMutableLiveData.value =
                DataWrapper(
                    CurrentData(null, null, "لطفا موقعیت یاب دستگاه را روشن کنید."),
                    DataStatus.DATA_RECEIVE_FAILURE
                )
        }
    }

    private fun getWeatherDataByLocation(location: Location?) {
        if (location != null)
            setUpObserver(currentWeatherRepository.getCurrentWeatherData("${location.latitude},${location.longitude}"))
    }

    private fun setUpObserver(liveData: LiveData<DataWrapper<CurrentData>>) {
        if (currentWeatherLiveData != null && currentWeatherLiveData!!.hasObservers())
            currentWeatherLiveData!!.removeObserver(dataObserver)
        currentWeatherLiveData = liveData
        currentWeatherLiveData!!.observeForever(dataObserver)
    }

    fun getCurrentWeatherLiveData(): MutableLiveData<DataWrapper<CurrentData>> =
        currentWeatherDataMutableLiveData

    override fun onCleared() {
        if (currentWeatherLiveData != null && currentWeatherLiveData!!.hasObservers())
            currentWeatherLiveData!!.removeObserver(dataObserver)
        super.onCleared()
    }
}