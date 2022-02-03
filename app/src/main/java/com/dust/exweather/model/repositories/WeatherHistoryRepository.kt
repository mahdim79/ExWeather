package com.dust.exweather.model.repositories

import androidx.lifecycle.LiveData
import com.dust.exweather.di.contributefragments.scopes.WeatherHistoryScope
import com.dust.exweather.model.csv.CsvFactory
import com.dust.exweather.model.dataclasses.historyweather.Forecastday
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.room.WeatherDao
import com.dust.exweather.model.room.WeatherEntity
import javax.inject.Inject

@WeatherHistoryScope
class WeatherHistoryRepository @Inject constructor() {
    @Inject
    lateinit var weatherDao: WeatherDao

    @Inject
    lateinit var csvFactory: CsvFactory

    fun getWeatherLiveDataFromCache(): LiveData<List<WeatherEntity>> =
        weatherDao.getLiveWeatherData()

    fun createWeatherCsvFile(forecastDay: Forecastday, location: String): DataWrapper<String> =
        csvFactory.createWeatherHistorySheet("exportDetails", forecastDay, location)

}