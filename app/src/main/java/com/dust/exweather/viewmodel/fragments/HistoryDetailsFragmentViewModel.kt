package com.dust.exweather.viewmodel.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.model.room.WeatherEntity

class HistoryDetailsFragmentViewModel(private val repository: CurrentWeatherRepository) :
    ViewModel() {
    fun getWeatherLiveDataFromCache(): LiveData<List<WeatherEntity>> =
        repository.getLiveWeatherDataFromCache()
}