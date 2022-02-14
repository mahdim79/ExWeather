package com.dust.exweather.viewmodel.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dust.exweather.model.dataclasses.location.LocationData
import com.dust.exweather.model.dataclasses.location.locationserverdata.LocationServerData
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.WeatherSettingsRepository
import com.dust.exweather.model.toDataClass
import com.dust.exweather.model.toEntity
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.utils.DataStatus
import com.dust.exweather.utils.UtilityFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.CancellationException

class WeatherSettingsViewModel(
    private val weatherSettingsRepository: WeatherSettingsRepository,
    private val sharedPreferencesManager: SharedPreferencesManager
) :
    ViewModel() {

    private val locationDetailsLiveData = MutableLiveData<DataWrapper<LocationServerData>>()

    private var locationDetailsJob: Job? = null

    suspend fun getLocationsData(): ArrayList<LocationData> {
        val locationDataList = arrayListOf<LocationData>()
        val dataList = weatherSettingsRepository.getAllWeatherData().map { it.toDataClass() }
        if (!dataList.isNullOrEmpty()) {

            var defaultLocation = sharedPreferencesManager.getDefaultLocation()
            if (defaultLocation.isNullOrEmpty()) {
                defaultLocation = dataList[0].location
                sharedPreferencesManager.setDefaultLocation(dataList[0].location)
            }

            dataList.forEach { mainWeatherData ->
                locationDataList.add(
                    LocationData(
                        mainWeatherData.current?.location?.name ?: "null",
                        mainWeatherData.location,
                        mainWeatherData.location == defaultLocation
                    )
                )
            }
        }

        return locationDataList
    }

    suspend fun removeLocation(latLong: String) {
        weatherSettingsRepository.removeLocation(latLong)
    }

    fun setDefaultLocation(latLong: String) {
        sharedPreferencesManager.setDefaultLocation(latLong)
    }

    fun getCurrentUserLocation(context: Context): String? {
        if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            var currentUserLocation = ""

            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    3000L,
                    0f
                ) { location ->
                    getLocationDetailsData("${location.latitude},${location.longitude}", context)
                }
            } else {
                return "لطفا مکان یاب دستگاه را روشن کنید و دوباره تلاش کنید"
            }

            if (currentUserLocation.isEmpty() && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    3000L,
                    0f
                ) { location ->
                    getLocationDetailsData("${location.latitude},${location.longitude}", context)
                }
            }

            return null
        }
        return "مشکلی پیش آمده است"
    }

    suspend fun insertLocationToCache(location: String): String {
        val currentData = weatherSettingsRepository.getDirectCachedData()
        currentData.forEach { data ->
            if (data.location == location)
                return "این مکان قبلا اضافه شده است!"
        }

        if (sharedPreferencesManager.getDefaultLocation().isNullOrEmpty())
            sharedPreferencesManager.setDefaultLocation(location)
        weatherSettingsRepository.addNewLocationToCache(
            arrayListOf(
                MainWeatherData(
                    current = null,
                    forecastDetailsData = null,
                    historyDetailsData = null,
                    location = location,
                    id = null
                ).toEntity()
            )
        )
        return ""
    }

    private fun getLocationDetailsData(latLng: String, context: Context) {
        locationDetailsJob?.cancel(CancellationException("Normal Cancellation!"))
        emitLoadingState()
        if (!UtilityFunctions.isNetworkConnectionEnabled(context)) {
            emitFailureState()
            return
        }
        locationDetailsJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val locationResponse = weatherSettingsRepository.getLocationDetailsData(latLng)
                if (locationResponse.isSuccessful && locationResponse.body() != null) {
                    locationResponse.body()?.let { locationServerData ->
                        withContext(Dispatchers.Main) {
                            emitSuccessfulState(locationServerData)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        emitFailureState()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    emitFailureState()
                }
            }
        }
    }

    private fun emitLoadingState() {
        locationDetailsLiveData.value = DataWrapper(null, DataStatus.DATA_RECEIVE_LOADING)
    }

    private fun emitFailureState() {
        locationDetailsLiveData.value =
            DataWrapper(null, DataStatus.DATA_RECEIVE_FAILURE)
    }

    private fun emitSuccessfulState(locationServerData: LocationServerData) {
        locationDetailsLiveData.value =
            DataWrapper(locationServerData, DataStatus.DATA_RECEIVE_SUCCESS)
    }

    fun getLocationDetailsLiveData(): LiveData<DataWrapper<LocationServerData>> =
        locationDetailsLiveData

}