package com.dust.exweather.viewmodel.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.model.room.WeatherEntity

class WeatherDetailsFragmentViewModel(private val repository: CurrentWeatherRepository) :
    ViewModel() {

    fun getWeatherDataFromCache(): LiveData<List<WeatherEntity>> =
        repository.getLiveWeatherDataFromCache()

    suspend fun getDirectWeatherDataFromCache():List<WeatherEntity> = repository.getDirectWeatherDataFromCache()
}