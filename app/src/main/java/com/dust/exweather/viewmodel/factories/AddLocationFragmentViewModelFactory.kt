package com.dust.exweather.viewmodel.factories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dust.exweather.model.repositories.AddLocationRepository
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.viewmodel.fragments.AddLocationFragmentViewModel
import javax.inject.Inject

class AddLocationFragmentViewModelFactory @Inject constructor(
    application: Application,
    private val addLocationRepository: AddLocationRepository,
    private val sharedPreferencesManager: SharedPreferencesManager
) :
    ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AddLocationFragmentViewModel(addLocationRepository, sharedPreferencesManager) as T
    }
}