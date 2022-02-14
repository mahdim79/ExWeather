package com.dust.exweather.model.repositories

import com.dust.exweather.model.dataclasses.location.locationserverdata.LocationServerData
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.retrofit.MainApiRequests
import com.dust.exweather.model.room.WeatherDao
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import retrofit2.Response
import javax.inject.Inject

class WeatherSettingsRepository @Inject constructor() {

    @Inject
    lateinit var weatherDao: WeatherDao

    @Inject
    lateinit var mainApiRequests:MainApiRequests

    suspend fun getAllWeatherData(): List<WeatherEntity> =
        weatherDao.getDirectWeatherData()


    suspend fun removeLocation(latLong: String) {
        val currentData = getAllWeatherData()
        for (i in currentData.indices) {
            if (currentData[i].toDataClass().location == latLong)
                weatherDao.removeWeatherData(currentData[i])
        }
    }

    suspend fun getLocationDetailsData(latLng: String): Response<LocationServerData> =
        mainApiRequests.getLocationDetailsData(latLng)

    suspend fun addNewLocationToCache(listEntities: List<WeatherEntity>) =
        weatherDao.insertWeatherData(listEntities)

    suspend fun getDirectCachedData(): List<MainWeatherData> =
        weatherDao.getDirectWeatherData().map { it.toDataClass() }

}