package com.dust.exweather.viewmodel.fragments

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dust.exweather.model.dataclasses.location.locationserverdata.LocationServerData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.AddLocationRepository
import com.dust.exweather.utils.DataStatus
import com.dust.exweather.utils.UtilityFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException

class AddLocationFragmentViewModel(private val addLocationRepository: AddLocationRepository) :
    ViewModel() {

    private val locationDetailsLiveData = MutableLiveData<DataWrapper<LocationServerData>>()

    private var locationDetailsJob: Job? = null

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

    private fun emitLoadingState() {
        locationDetailsLiveData.value = DataWrapper(null, DataStatus.DATA_RECEIVE_LOADING)
    }

    private fun emitFailureState() {
        locationDetailsLiveData.value =
            DataWrapper(null, DataStatus.DATA_RECEIVE_FAILURE)
    }

    private fun emitSuccessfulState(locationServerData:LocationServerData) {
        locationDetailsLiveData.value =
            DataWrapper(locationServerData, DataStatus.DATA_RECEIVE_SUCCESS)
    }

    fun getLocationDetailsLiveData(): LiveData<DataWrapper<LocationServerData>> =
        locationDetailsLiveData

}