package com.dust.exweather.model.retrofit

import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.utils.Constants
import io.reactivex.Flowable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MainApiRequests {
    @GET("current.json?key=${Constants.API_KEY}")
    suspend fun getCurrentWeatherData(@Query("q") cityName: String , @Query("api") api:String = "no"): Response<CurrentData>
}