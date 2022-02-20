package com.dust.exweather.viewmodel.fragments

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dust.exweather.R
import com.dust.exweather.model.DataOptimizer
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasses.forecastweather.Forecastday
import com.dust.exweather.model.dataclasses.forecastweather.WeatherForecast
import com.dust.exweather.model.dataclasses.historyweather.WeatherHistory
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import com.dust.exweather.utils.DataStatus
import com.dust.exweather.utils.UtilityFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class CurrentDetailsFragmentViewModel(
    private val currentWeatherRepository: CurrentWeatherRepository
) :
    ViewModel() {

    fun getWeatherDataFromApi(context: Context){
        currentWeatherRepository.doApiCall(context)
    }

    fun getWeatherApiCallStateLiveData(): LiveData<DataWrapper<String>> =
        currentWeatherRepository.getWeatherApiCallStateLiveData()

    fun getLiveWeatherDataFromCache(): LiveData<List<WeatherEntity>> =
        currentWeatherRepository.getLiveWeatherDataFromCache()


}