package com.dust.exweather.model.repositories

import androidx.lifecycle.LiveData
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasses.forecastweather.WeatherForecast
import com.dust.exweather.model.dataclasses.historyweather.WeatherHistory
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.dataclasses.translatedataclass.TranslationDataClass
import com.dust.exweather.model.retrofit.MainApiRequests
import com.dust.exweather.model.retrofit.TranslationApiRequests
import com.dust.exweather.model.room.WeatherDao
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toEntity
import retrofit2.Response
import javax.inject.Inject
class CurrentWeatherRepository @Inject constructor() {
    @Inject
    lateinit var mainApiRequests: MainApiRequests

    @Inject
    lateinit var translationApiRequests: TranslationApiRequests

    @Inject
    lateinit var weatherDao: WeatherDao

    suspend fun getCurrentWeatherData(query: String): Response<CurrentData> = mainApiRequests.getCurrentWeatherData(query)
    suspend fun getHistoryWeatherData(query: String, date:String): Response<WeatherHistory> = mainApiRequests.getHistoryWeatherData(query , date)
    suspend fun getForecastWeatherData(query: String): Response<WeatherForecast> = mainApiRequests.getForecastWeatherData(query)

    suspend fun insertWeatherDataToRoom(mainWeatherData: List<MainWeatherData>) {
        weatherDao.insertWeatherData(mainWeatherData.map {
            it.toEntity()
        })
    }

    suspend fun translateWord(text: String): Response<TranslationDataClass> =
        translationApiRequests.translate(text = text)

    suspend fun getDirectWeatherDataFromCache(): List<WeatherEntity> =
        weatherDao.getDirectWeatherData()

    fun getLiveWeatherDataFromCache():LiveData<List<WeatherEntity>> = weatherDao.getLiveWeatherData()


}

