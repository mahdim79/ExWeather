package com.dust.exweather.model.repositories

import com.dust.exweather.model.dataclasses.location.SearchLocation
import com.dust.exweather.model.dataclasses.location.locationserverdata.LocationServerData
import com.dust.exweather.model.retrofit.MainApiRequests
import com.dust.exweather.model.room.WeatherDao
import com.dust.exweather.model.room.WeatherEntity
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class AddLocationRepository @Inject constructor() {
    @Inject
    lateinit var mainApiRequests: MainApiRequests

    @Inject
    lateinit var weatherDao: WeatherDao

    suspend fun getLocationDetailsData(latLng: String): Response<LocationServerData> =
        mainApiRequests.getLocationDetailsData(latLng)

    suspend fun searchForLocation(text: String): Response<ArrayList<SearchLocation>> =
        mainApiRequests.searchForLocation(text)

    suspend fun addNewLocationToCache(listEntities: List<WeatherEntity>) =
        weatherDao.insertWeatherData(listEntities)
}