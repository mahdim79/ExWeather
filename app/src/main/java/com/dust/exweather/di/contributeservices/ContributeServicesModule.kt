package com.dust.exweather.di.contributeservices

import com.dust.exweather.service.NotificationService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ContributeServicesModule {
    @ContributesAndroidInjector
    abstract fun contributeNotificationService():NotificationService
}