package com.dust.exweather.viewmodel.factories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dust.exweather.model.repositories.WeatherHistoryRepository
import com.dust.exweather.viewmodel.fragments.HistoryFragmentViewModel
import javax.inject.Inject

class HistoryFragmentViewModelFactory @Inject constructor(
    val application: Application,
    private val repository: WeatherHistoryRepository
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HistoryFragmentViewModel(repository) as T
    }
}