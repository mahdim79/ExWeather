package com.dust.exweather.viewmodel.fragments

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dust.exweather.model.dataclasses.location.SearchLocation
import com.dust.exweather.model.dataclasses.location.locationserverdata.LocationServerData
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.AddLocationRepository
import com.dust.exweather.model.toEntity
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.utils.DataStatus
import com.dust.exweather.utils.UtilityFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.CancellationException

class AddLocationFragmentViewModel(
    private val addLocationRepository: AddLocationRepository,
    private val sharedPreferencesManager: SharedPreferencesManager
) :
    ViewModel() {

    private val locationDetailsLiveData = MutableLiveData<DataWrapper<LocationServerData>>()

    private val manualLocationPickerFragmentLiveData =
        MutableLiveData<DataWrapper<ArrayList<SearchLocation>>>()

    private var locationDetailsJob: Job? = null

    private var searchLocationJob: Job? = null

    fun getLocationDetailsData(latLng: String, context: Context) {
        locationDetailsJob?.cancel(CancellationException("Normal Cancellation!"))
        emitLoadingState()
        if (!UtilityFunctions.isNetworkConnectionEnabled(context)) {
            emitFailureState()
            return
        }
        locationDetailsJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val locationResponse = addLocationRepository.getLocationDetailsData(latLng)
                if (locationResponse.isSuccessful && locationResponse.body() != null) {
                    locationResponse.body()?.let { locationServerData ->
                        withContext(Dispatchers.Main) {
                            emitSuccessfulState(locationServerData)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        emitFailureState()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    emitFailureState()
                }
            }
        }
    }

    fun searchForLocation(text: String, context: Context) {
        searchLocationJob?.cancel(CancellationException("Normal Cancellation!"))
        emitLoadingSearch()
        if (!UtilityFunctions.isNetworkConnectionEnabled(context)) {
            emitFailureSearch()
            return
        }
        searchLocationJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val searchResponse = addLocationRepository.searchForLocation(text)
                if (searchResponse.isSuccessful && searchResponse.body() != null) {
                    searchResponse.body()?.let { searchData ->
                        withContext(Dispatchers.Main) {
                            emitSuccessfulSearch(searchData)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        emitFailureSearch()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    emitFailureSearch()
                }
            }
        }
    }

    suspend fun insertLocationToCache(location: String): String {
        val currentData = getDirectCachedData()
        currentData.forEach { data ->
            if (data.location == location)
                return "این مکان قبلا اضافه شده است!"
        }

        if (sharedPreferencesManager.getDefaultLocation().isNullOrEmpty())
            sharedPreferencesManager.setDefaultLocation(location)
        addLocationRepository.addNewLocationToCache(
            arrayListOf(
                MainWeatherData(
                    current = null,
                    forecastDetailsData = null,
                    historyDetailsData = null,
                    location = location,
                    id = null
                ).toEntity()
            )
        )
        return ""
    }

    suspend fun getDirectCachedData(): List<MainWeatherData> =
        addLocationRepository.getDirectCachedData()

    private fun emitFailureSearch() {
        manualLocationPickerFragmentLiveData.value =
            DataWrapper(null, DataStatus.DATA_RECEIVE_FAILURE)
    }

    private fun emitLoadingSearch() {
        manualLocationPickerFragmentLiveData.value =
            DataWrapper(null, DataStatus.DATA_RECEIVE_LOADING)

    }

    private fun emitSuccessfulSearch(data: ArrayList<SearchLocation>) {
        manualLocationPickerFragmentLiveData.value =
            DataWrapper(data, DataStatus.DATA_RECEIVE_SUCCESS)
    }

    private fun emitLoadingState() {
        locationDetailsLiveData.value = DataWrapper(null, DataStatus.DATA_RECEIVE_LOADING)
    }

    private fun emitFailureState() {
        locationDetailsLiveData.value =
            DataWrapper(null, DataStatus.DATA_RECEIVE_FAILURE)
    }

    private fun emitSuccessfulState(locationServerData: LocationServerData) {
        locationDetailsLiveData.value =
            DataWrapper(locationServerData, DataStatus.DATA_RECEIVE_SUCCESS)
    }

    fun getLocationDetailsLiveData(): LiveData<DataWrapper<LocationServerData>> =
        locationDetailsLiveData

    fun getManualLocationPickerFragmentLiveData(): MutableLiveData<DataWrapper<ArrayList<SearchLocation>>> =
        manualLocationPickerFragmentLiveData

}