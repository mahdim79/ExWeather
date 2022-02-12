package com.dust.exweather.viewmodel.fragments

import androidx.lifecycle.ViewModel
import com.dust.exweather.model.dataclasses.location.LocationData
import com.dust.exweather.model.repositories.WeatherSettingsRepository
import com.dust.exweather.model.toDataClass
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import java.util.*

class WeatherSettingsViewModel(
    private val weatherSettingsRepository: WeatherSettingsRepository,
    private val sharedPreferencesManager: SharedPreferencesManager
) :
    ViewModel() {

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

}