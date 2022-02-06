package com.dust.exweather.di.contributeactivities

import com.dust.exweather.di.contributeactivities.scopes.MainActivityScope
import com.dust.exweather.di.contributefragments.ContributeMainFragments
import com.dust.exweather.ui.activities.MainActivity
import com.dust.exweather.ui.activities.SplashActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ContributeActivitiesModule {
    @ContributesAndroidInjector(modules = [ContributeMainFragments::class, MainActivityModule::class])
    @MainActivityScope
    abstract fun contributeMainActivity():MainActivity

    @ContributesAndroidInjector
    abstract fun contributeSplashActivity():SplashActivity
}