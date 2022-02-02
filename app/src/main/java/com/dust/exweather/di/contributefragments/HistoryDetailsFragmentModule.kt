package com.dust.exweather.di.contributefragments

import android.app.Application
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.viewmodel.factories.HistoryDetailsViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class HistoryDetailsFragmentModule {
    @Provides
    fun provideHistoryDetailsViewModelFactory(
        application: Application,
        repository: CurrentWeatherRepository
    ): HistoryDetailsViewModelFactory {
        return HistoryDetailsViewModelFactory(application, repository)
    }
}