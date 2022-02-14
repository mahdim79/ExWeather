package com.dust.exweather.model.repositories

import com.dust.exweather.model.room.WeatherDao
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import javax.inject.Inject

class WeatherSettingsRepository @Inject constructor() {

    @Inject
    lateinit var weatherDao: WeatherDao

    suspend fun getAllWeatherData(): List<WeatherEntity> =
        weatherDao.getDirectWeatherData()


    suspend fun removeLocation(latLong: String) {
        val currentData = getAllWeatherData()
        for (i in currentData.indices) {
            if (currentData[i].toDataClass().location == latLong)
                weatherDao.removeWeatherData(currentData[i])
        }
    }
}