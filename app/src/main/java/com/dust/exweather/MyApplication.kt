package com.dust.exweather

import android.app.Application
import com.dust.exweather.di.singletoncomponent.DaggerSingletonComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class MyApplication :DaggerApplication(){
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerSingletonComponent.builder().provideApplication(this).build()
    }

}