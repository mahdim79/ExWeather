package com.dust.exweather.model.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.retrofit.MainApiRequests
import com.dust.exweather.model.room.CurrentWeatherDao
import com.dust.exweather.model.room.CurrentWeatherEntity
import com.dust.exweather.model.toEntity
import com.dust.exweather.utils.DataStatus
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class CurrentWeatherRepository @Inject constructor() {
    @Inject
    lateinit var mainApiRequests: MainApiRequests

    @Inject
    lateinit var currentWeatherDao: CurrentWeatherDao

    fun getCurrentWeatherData(query: String): LiveData<DataWrapper<CurrentData>> {
        return LiveDataReactiveStreams.fromPublisher(
            mainApiRequests.getCurrentWeatherData(query)
                .onErrorReturn {
                    CurrentData(null, null , it.message ?:"null")
                }
                .map { data ->
                    if (data.current == null) {
                        return@map DataWrapper<CurrentData>(null, DataStatus.DATA_RECEIVE_FAILURE)
                    } else {
                        insertCurrentWeatherDataToRoom(data)
                        return@map DataWrapper(data, DataStatus.DATA_RECEIVE_SUCCESS)
                    }
                }
                .subscribeOn(Schedulers.io())
        )
    }

    private fun insertCurrentWeatherDataToRoom(currentData: CurrentData){
        CoroutineScope(Dispatchers.IO).launch{
            currentWeatherDao.insertCurrentWeather(currentWeatherEntity = currentData.toEntity())
        }
    }

    fun getCurrentWeatherDataFromRoom():LiveData<List<CurrentWeatherEntity>> = currentWeatherDao.getCurrentWeatherData()


}