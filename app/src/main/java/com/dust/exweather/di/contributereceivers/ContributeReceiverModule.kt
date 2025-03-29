package com.dust.exweather.di.contributereceivers

import com.dust.exweather.broadcast.WidgetUpdaterReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ContributeReceiverModule {
    @ContributesAndroidInjector
    abstract fun contributeWidgetUpdaterReceiver(): WidgetUpdaterReceiver
}