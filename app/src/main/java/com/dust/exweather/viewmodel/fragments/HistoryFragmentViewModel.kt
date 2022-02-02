package com.dust.exweather.viewmodel.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dust.exweather.model.dataclasses.historyweather.WeatherHistory
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import java.util.*

class HistoryFragmentViewModel(private val repository: CurrentWeatherRepository) : ViewModel() {

    private val historyDataList = arrayListOf<WeatherHistory>()

    fun getLiveWeatherDataFromCache(): LiveData<List<WeatherEntity>> =
        repository.getLiveWeatherDataFromCache()

    fun getHistoryDataList(): ArrayList<WeatherHistory> = historyDataList

    fun addHistoryDataToDataList(rawData: List<WeatherEntity>) {
        val data = rawData.map { it.toDataClass() }
        data.forEach { mainWeatherData ->
            mainWeatherData.historyDetailsData?.let { weatherHistory ->
                historyDataList.add(weatherHistory)
            }
        }
    }

}