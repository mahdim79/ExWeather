package com.dust.exweather.di.contributeactivities

import com.dust.exweather.di.contributefragments.ContributeMainFragments
import com.dust.exweather.ui.activities.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ContributeActivitiesModule {
    @ContributesAndroidInjector(modules = [ContributeMainFragments::class])
    abstract fun contributeMainActivity():MainActivity
}