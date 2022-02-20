package com.dust.exweather.viewmodel.factories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dust.exweather.model.DataOptimizer
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.viewmodel.fragments.ForecastDetailsFragmentViewModel
import javax.inject.Inject

class ForecastDetailsViewModelFactory @Inject constructor(
    application: Application,
    private val currentWeatherRepository: CurrentWeatherRepository,
    private val dataOptimizer:DataOptimizer
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ForecastDetailsFragmentViewModel(currentWeatherRepository,dataOptimizer) as T
    }
}