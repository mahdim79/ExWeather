package com.dust.exweather.viewmodel.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.historyweather.Forecast
import com.dust.exweather.model.dataclasses.historyweather.WeatherHistory
import com.dust.exweather.model.dataclasses.location.LocationData
import com.dust.exweather.model.dataclasses.location.locationserverdata.LocationServerData
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.WeatherSettingsRepository
import com.dust.exweather.model.toDataClass
import com.dust.exweather.model.toEntity
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.utils.DataStatus
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.CancellationException

class WeatherSettingsViewModel(
    private val weatherSettingsRepository: WeatherSettingsRepository,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val locationManager: LocationManager
) :
    ViewModel(), LocationListener {

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
                        mainWeatherData.current?.location?.name
                            ?: mainWeatherData.historyDetailsData?.location?.name ?: "",
                        mainWeatherData.location,
                        mainWeatherData.location == defaultLocation
                    )
                )
            }
        }

        return locationDataList
    }

    suspend fun removeLocation(latLong: String, context: Context) {
        weatherSettingsRepository.removeLocation(latLong, context)
    }

    fun setDefaultLocation(latLong: String) {
        sharedPreferencesManager.setDefaultLocation(latLong)
    }

    fun getCurrentUserLocation(context: Context): String? {
        if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    3000L,
                    0f,
                    this
                )
            } else {
                return context.getString(R.string.turnOnGps)
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    3000L,
                    0f,
                    this
                )
            }

            return null
        }
        return context.getString(R.string.problem)
    }

    suspend fun insertLocationToCache(latLng: LatLng, locationName: String, context: Context): String {
        val currentData = weatherSettingsRepository.getDirectCachedData()
        val location = "${latLng.latitude},${latLng.longitude}"
        currentData.forEach { data ->
            if (data.location == location)
                return context.getString(R.string.alreadyAdded)
        }

        if (sharedPreferencesManager.getDefaultLocation().isNullOrEmpty())
            sharedPreferencesManager.setDefaultLocation(location)
        weatherSettingsRepository.addNewLocationToCache(
            arrayListOf(
                MainWeatherData(
                    current = null,
                    forecastDetailsData = null,
                    historyDetailsData = WeatherHistory(
                        Forecast(arrayListOf()),
                        location = com.dust.exweather.model.dataclasses.currentweather.main.Location(
                            "",
                            latLng.latitude,
                            "",
                            0,
                            latLng.longitude,
                            locationName,
                            "",
                            ""
                        )
                    ),
                    location = location,
                    id = null
                ).toEntity()
            )
        )
        return ""
    }

    private fun getLocationDetailsData(latLng: String) {
        locationDetailsJob?.cancel(CancellationException("Normal Cancellation!"))
        emitLoadingState()
        locationDetailsJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val locationResponse = weatherSettingsRepository.getLocationDetailsData(latLng)
                if (locationResponse?.body() != null && locationResponse.isSuccessful) {
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

    override fun onLocationChanged(location: Location) {
        getLocationDetailsData("${location.latitude},${location.longitude}")
    }

    override fun onProviderEnabled(provider: String) {
        super.onProviderEnabled(provider)
    }

    override fun onProviderDisabled(provider: String) {
        // super.onProviderDisabled(provider)
    }

}