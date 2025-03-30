package com.dust.exweather

import android.graphics.Typeface
import com.dust.exweather.di.singletoncomponent.DaggerSingletonComponent
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.utils.Constants
import com.dust.exweather.utils.Settings
import com.google.firebase.FirebaseApp
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import java.util.*
import javax.inject.Inject

class MyApplication : DaggerApplication() {
    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        initAppCenter()
        getTypeFace()
    }

    private fun initAppCenter() {
        AppCenter.start(this,Constants.APP_CENTER_DSN,Analytics::class.java,Crashes::class.java)
    }

    fun getCurrentThemeResId(): Int {
        return if (sharedPreferencesManager.getThemeSettings() == Settings.THEME_LIGHT) Constants.LIGHT_THEME_RES_ID else Constants.DARK_THEME_RES_ID
    }
    
    fun getCurrentLocaleStr():String{
        return if (sharedPreferencesManager.getLanguageSettings() == Settings.LANGUAGE_ENGLISH)
            "en"
        else
            "fa"
    }

    fun getTypeFace(): Typeface {
        return Typeface.createFromAsset(assets, "fonts/iraniansans.ttf")
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerSingletonComponent.builder().provideApplication(this).build()
    }

}