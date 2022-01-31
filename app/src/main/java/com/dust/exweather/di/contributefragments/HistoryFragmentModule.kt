package com.dust.exweather.di.contributefragments

import android.app.Application
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.viewmodel.factories.HistoryFragmentViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class HistoryFragmentModule {
    @Provides
    fun provideHistoryFragmentModule(
        application: Application,
        currentWeatherRepository: CurrentWeatherRepository
    ):HistoryFragmentViewModelFactory {
        return HistoryFragmentViewModelFactory(application, currentWeatherRepository)
    }
}