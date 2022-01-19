package com.dust.exweather.viewmodel.factories

import android.app.Application
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.viewmodel.fragments.CurrentFragmentViewModel
import javax.inject.Inject

class CurrentFragmentViewModelFactory @Inject constructor(
    application: Application,
    val currentWeatherRepository: CurrentWeatherRepository,
    val locationManager: LocationManager
) : AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CurrentFragmentViewModel(currentWeatherRepository, locationManager) as T
    }
}