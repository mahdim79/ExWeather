package com.dust.exweather

import android.graphics.Typeface
import com.dust.exweather.di.singletoncomponent.DaggerSingletonComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class MyApplication : DaggerApplication() {
    override fun onCreate() {
        super.onCreate()
        getTypeFace()
    }

    public fun getTypeFace():Typeface {
        return Typeface.createFromAsset(assets , "fonts/iraniansans.ttf")
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerSingletonComponent.builder().provideApplication(this).build()
    }

}