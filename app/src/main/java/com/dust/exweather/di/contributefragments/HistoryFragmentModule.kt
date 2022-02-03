package com.dust.exweather.di.contributefragments

import android.app.Application
import com.dust.exweather.model.repositories.WeatherHistoryRepository
import com.dust.exweather.viewmodel.factories.HistoryFragmentViewModelFactory
import dagger.Module
import dagger.Provides
import org.apache.poi.hssf.usermodel.HSSFWorkbook

@Module
class HistoryFragmentModule {
    @Provides
    fun provideHistoryFragmentViewModelFactory(
        application: Application,
        repo: WeatherHistoryRepository
    ): HistoryFragmentViewModelFactory {
        return HistoryFragmentViewModelFactory(application, repo)
    }

}