package com.dust.exweather.model.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasses.translatedataclass.TranslationDataClass
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.retrofit.MainApiRequests
import com.dust.exweather.model.retrofit.TranslationApiRequests
import com.dust.exweather.model.room.CurrentWeatherDao
import com.dust.exweather.model.room.CurrentWeatherEntity
import com.dust.exweather.model.toEntity
import com.dust.exweather.utils.DataStatus
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject
class CurrentWeatherRepository @Inject constructor() {
    @Inject
    lateinit var mainApiRequests: MainApiRequests

    @Inject
    lateinit var translationApiRequests: TranslationApiRequests

    @Inject
    lateinit var currentWeatherDao: CurrentWeatherDao

    suspend fun getCurrentWeatherData(query: String): Response<CurrentData> = mainApiRequests.getCurrentWeatherData(query)

    suspend fun insertCurrentWeatherDataToRoom(currentData: CurrentData) {
        currentWeatherDao.insertCurrentWeather(currentWeatherEntity = currentData.toEntity())
    }

    suspend fun translateWord(text: String): Response<TranslationDataClass> =
        translationApiRequests.translate(text = text)

    fun getCurrentWeatherDataFromRoom(): LiveData<List<CurrentWeatherEntity>> =
        currentWeatherDao.getCurrentWeatherData()


}

