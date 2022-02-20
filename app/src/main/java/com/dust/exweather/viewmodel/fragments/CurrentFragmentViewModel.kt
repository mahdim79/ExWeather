package com.dust.exweather.viewmodel.fragments

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dust.exweather.model.DataOptimizer
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.utils.DataStatus

class CurrentFragmentViewModel(
    private val currentWeatherRepository: CurrentWeatherRepository
) : ViewModel() {

    private val detailsViewPagerProgressStateLiveData = MutableLiveData<Boolean>()

    fun getWeatherDataFromApi(context: Context){
        currentWeatherRepository.doApiCall(context)
    }

    fun getWeatherApiCallStateLiveData(): LiveData<DataWrapper<String>> =
        currentWeatherRepository.getWeatherApiCallStateLiveData()

    fun calculateMainRecyclerViewDataList(data: MainWeatherData): List<DataWrapper<Any>> {
        val list = arrayListOf<DataWrapper<Any>>()
        data.forecastDetailsData!!.forecast.forecastday.forEach {
            list.add(DataWrapper(it, DataStatus.DATA_RECEIVE_SUCCESS))
        }

        list.reverse()

        list.add(DataWrapper(data.current!!, DataStatus.DATA_RECEIVE_SUCCESS))

        data.historyDetailsData!!.forecast.forecastday.forEach {
            list.add(DataWrapper(it, DataStatus.DATA_RECEIVE_SUCCESS))
        }

        return list
    }

    suspend fun getDirectWeatherDataFromCache(): List<WeatherEntity> =
        currentWeatherRepository.getDirectWeatherDataFromCache()

    fun getLiveWeatherDataFromCache(): LiveData<List<WeatherEntity>> =
        currentWeatherRepository.getLiveWeatherDataFromCache()


    fun getDetailsViewPagerProgressStateLiveData(): LiveData<Boolean> =
        detailsViewPagerProgressStateLiveData

    fun setDetailsViewPagerProgressState(b: Boolean) {
        detailsViewPagerProgressStateLiveData.value = b
    }

}