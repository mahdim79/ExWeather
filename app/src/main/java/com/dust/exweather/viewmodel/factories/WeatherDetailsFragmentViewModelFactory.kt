package com.dust.exweather.viewmodel.factories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.viewmodel.fragments.WeatherDetailsFragmentViewModel
import javax.inject.Inject

class WeatherDetailsFragmentViewModelFactory @Inject constructor(
    application: Application,
    private val repository: CurrentWeatherRepository
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WeatherDetailsFragmentViewModel(repository) as T
    }
}