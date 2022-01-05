package com.dust.exweather.di.singletoncomponent

import android.app.Application
import com.dust.exweather.MyApplication
import com.dust.exweather.di.contributeactivities.ContributeActivitiesModule
import com.dust.exweather.di.singletoncomponent.modules.SingletonComponentMainModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, ContributeActivitiesModule::class, SingletonComponentMainModule::class])
interface SingletonComponent : AndroidInjector<MyApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun provideApplication(application: Application): Builder
        fun build(): SingletonComponent
    }
}