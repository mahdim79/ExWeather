package com.dust.exweather

import android.graphics.Typeface
import com.dust.exweather.di.singletoncomponent.DaggerSingletonComponent
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.utils.Constants
import com.dust.exweather.utils.Settings
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import javax.inject.Inject

class MyApplication : DaggerApplication() {
    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate() {
        super.onCreate()
        getTypeFace()
        getCurrentThemeResId()
    }

    fun getCurrentThemeResId(): Int {
        return if (sharedPreferencesManager.getThemeSettings() == Settings.THEME_LIGHT) Constants.LIGHT_THEME_RES_ID else Constants.DARK_THEME_RES_ID
    }

    fun getTypeFace(): Typeface {
        return Typeface.createFromAsset(assets, "fonts/iraniansans.ttf")
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerSingletonComponent.builder().provideApplication(this).build()
    }

}