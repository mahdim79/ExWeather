package com.dust.exweather.di.singletoncomponent

import android.app.Application
import com.dust.exweather.MyApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@Component(modules = [AndroidSupportInjectionModule::class])
interface SingletonComponent : AndroidInjector<MyApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun provideApplication(application: Application): Builder
        fun build(): SingletonComponent
    }
}