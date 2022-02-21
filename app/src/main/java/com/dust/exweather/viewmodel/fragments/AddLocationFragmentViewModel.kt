package com.dust.exweather.viewmodel.fragments

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.currentweather.main.Location
import com.dust.exweather.model.dataclasses.historyweather.Forecast
import com.dust.exweather.model.dataclasses.historyweather.WeatherHistory
import com.dust.exweather.model.dataclasses.location.SearchLocation
import com.dust.exweather.model.dataclasses.location.locationserverdata.LocationServerData
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.AddLocationRepository
import com.dust.exweather.model.toEntity
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.utils.DataStatus
import com.dust.exweather.utils.UtilityFunctions
import com.dust.exweather.widget.WidgetUpdater
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

    suspend fun insertLocationToCache(locationText: String, widgetUpdater: WidgetUpdater, context: Context): String {

        try {
            var calculatedLat =
                locationText.substring(locationText.indexOf("|") + 1, locationText.indexOf(","))
            var calculatedLon =
                locationText.substring(locationText.indexOf(","), locationText.lastIndex)

            if (calculatedLat.endsWith("."))
                calculatedLat = calculatedLat.replace(".", "")

            if (calculatedLat.contains(" "))
                calculatedLat = calculatedLat.replace(" ", "")

            if (calculatedLon.endsWith("."))
                calculatedLon = calculatedLon.replace(".", "")

            if (calculatedLat.contains(" "))
                calculatedLat = calculatedLat.replace(" ", "")

            if (calculatedLat.startsWith(","))
                calculatedLat = calculatedLat.replace(",", "")

            if (calculatedLon.startsWith(","))
                calculatedLon = calculatedLon.replace(",", "")

            var latLangString = "$calculatedLat,$calculatedLon"

            val currentData = getDirectCachedData()
            currentData.forEach { data ->
                if (data.location == latLangString)
                    return context.getString(R.string.alreadyAdded)
            }

            addLocationRepository.addNewLocationToCache(
                arrayListOf(
                    MainWeatherData(
                        current = null,
                        forecastDetailsData = null,
                        WeatherHistory(
                            Forecast(arrayListOf()),
                            location = Location(
                                "",
                                calculatedLat.toDouble(),
                                "",
                                0,
                                calculatedLon.toDouble(),
                                locationText.substring(0, locationText.indexOf("|") - 1),
                                "",
                                ""
                            )
                        ),
                        location = latLangString,
                        id = null
                    ).toEntity()
                )
            )

            if (sharedPreferencesManager.getDefaultLocation().isNullOrEmpty()) {
                sharedPreferencesManager.setDefaultLocation(latLangString)
                // if there is no default location and this is the first location, application widget should be updated

                withContext(Dispatchers.Main) {
                    widgetUpdater.updateWidget()
                }
            }

        } catch (e: Exception) {
            return context.getString(R.string.addCorrectLocation)
        }
        return ""
    }

    private suspend fun getDirectCachedData(): List<MainWeatherData> =
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