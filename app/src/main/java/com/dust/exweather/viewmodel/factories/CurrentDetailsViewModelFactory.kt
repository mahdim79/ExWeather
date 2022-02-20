package com.dust.exweather.viewmodel.factories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dust.exweather.model.DataOptimizer
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.viewmodel.fragments.CurrentDetailsFragmentViewModel
import javax.inject.Inject

class CurrentDetailsViewModelFactory @Inject constructor(
    application: Application,
    private val repository: CurrentWeatherRepository,
    private val dataOptimizer: DataOptimizer
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CurrentDetailsFragmentViewModel(repository,dataOptimizer) as T
    }
}