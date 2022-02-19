package com.dust.exweather.viewmodel.factories

import android.app.Application
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dust.exweather.model.repositories.WeatherSettingsRepository
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.viewmodel.fragments.WeatherSettingsViewModel
import javax.inject.Inject

class WeatherSettingsViewModelFactory @Inject constructor(
    application: Application,
    private val weatherSettingsRepository: WeatherSettingsRepository,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val locationManager: LocationManager
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WeatherSettingsViewModel(
            weatherSettingsRepository = weatherSettingsRepository,
            sharedPreferencesManager = sharedPreferencesManager,
            locationManager = locationManager
        ) as T
    }
}