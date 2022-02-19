package com.dust.exweather.viewmodel.factories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.viewmodel.fragments.GeneralSettingsViewModel
import javax.inject.Inject

class GeneralSettingsViewModelFactory @Inject constructor(
    application: Application,
    private val sharedPreferencesManager: SharedPreferencesManager
) :
    ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return GeneralSettingsViewModel(sharedPreferencesManager) as T
    }
}