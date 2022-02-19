package com.dust.exweather.di.contributeactivities.modules

import android.app.Application
import android.content.Context
import android.location.LocationManager
import dagger.Module
import dagger.Provides

@Module
class MainActivityModule {
    @Provides
    fun provideLocationManager(application: Application): LocationManager {
        return application.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
}