package com.dust.exweather.viewmodel.factories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.viewmodel.fragments.HistoryFragmentViewModel

class HistoryFragmentViewModelFactory(
    val application: Application,
    private val repository: CurrentWeatherRepository
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HistoryFragmentViewModel(repository) as T
    }
}