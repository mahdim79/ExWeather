package com.dust.exweather.model.repositories

import com.dust.exweather.model.dataclasses.location.locationserverdata.LocationServerData
import com.dust.exweather.model.retrofit.MainApiRequests
import retrofit2.Response
import javax.inject.Inject

class AddLocationRepository @Inject constructor() {
    @Inject
    lateinit var mainApiRequests: MainApiRequests

    suspend fun getLocationDetailsData(latLng: String): Response<LocationServerData> =
        mainApiRequests.getLocationDetailsData(latLng)
}