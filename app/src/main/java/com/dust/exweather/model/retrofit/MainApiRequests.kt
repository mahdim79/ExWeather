package com.dust.exweather.model.retrofit

import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasses.forecastweather.WeatherForecast
import com.dust.exweather.model.dataclasses.historyweather.WeatherHistory
import com.dust.exweather.model.dataclasses.location.SearchLocation
import com.dust.exweather.model.dataclasses.location.locationserverdata.LocationServerData
import com.dust.exweather.utils.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.ArrayList

interface MainApiRequests {
    @GET("current.json?key=${Constants.API_KEY}")
    suspend fun getCurrentWeatherData(
        @Query("q") cityName: String,
        @Query("api") api: String = "no"
    ): Response<CurrentData>

    @GET("history.json?key=${Constants.API_KEY}")
    suspend fun getHistoryWeatherData(
        @Query("q") cityName: String,
        @Query("dt") date: String
    ): Response<WeatherHistory>

    @GET("forecast.json?key=${Constants.API_KEY}")
    suspend fun getForecastWeatherData(
        @Query("q") cityName: String,
        @Query("days") days: Int = 10,
        @Query("aqi") aqi: String = "yes",
        @Query("alerts") alerts: String = "no"
    ): Response<WeatherForecast>

    @GET("timezone.json?key=${Constants.API_KEY}")
    suspend fun getLocationDetailsData(@Query("q") locationOrLatLng: String): Response<LocationServerData>

    @GET("search.json?key=${Constants.API_KEY}")
    suspend fun searchForLocation(@Query("q") text: String): Response<ArrayList<SearchLocation>>
}