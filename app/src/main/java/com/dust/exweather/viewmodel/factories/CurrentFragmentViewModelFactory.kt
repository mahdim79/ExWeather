package com.dust.exweather.viewmodel.factories

import android.app.Application
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.model.room.CityDao
import com.dust.exweather.viewmodel.fragments.CurrentFragmentViewModel

class CurrentFragmentViewModelFactory(
    application: Application,
    val currentWeatherRepository: CurrentWeatherRepository,
    val cityDao: CityDao,
    val locationManager: LocationManager
) : AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CurrentFragmentViewModel(currentWeatherRepository, cityDao, locationManager) as T
    }
}