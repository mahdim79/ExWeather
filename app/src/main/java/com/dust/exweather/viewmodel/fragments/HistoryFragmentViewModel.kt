package com.dust.exweather.viewmodel.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dust.exweather.model.dataclasses.historyweather.Forecastday
import com.dust.exweather.model.dataclasses.historyweather.WeatherHistory
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.WeatherHistoryRepository
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import java.util.*

class HistoryFragmentViewModel(private val repository: WeatherHistoryRepository) : ViewModel() {

    private val historyDataList = arrayListOf<WeatherHistory>()

    fun getLiveWeatherDataFromCache(): LiveData<List<WeatherEntity>> =
        repository.getWeatherLiveDataFromCache()

    fun getHistoryDataList(): ArrayList<WeatherHistory> = historyDataList

    fun addHistoryDataToDataList(rawData: List<WeatherEntity>) {
        val data = rawData.map { it.toDataClass() }
        data.forEach { mainWeatherData ->
            mainWeatherData.historyDetailsData?.let { weatherHistory ->
                historyDataList.add(weatherHistory)
            }
        }
    }

    fun exportToCsvFile(forecastDay: Forecastday, location: String, fileName:String): DataWrapper<String> =
        repository.createWeatherCsvFile(forecastDay, location, fileName)

}