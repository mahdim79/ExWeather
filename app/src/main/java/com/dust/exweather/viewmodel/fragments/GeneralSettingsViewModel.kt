package com.dust.exweather.viewmodel.fragments

import android.content.Context
import androidx.lifecycle.ViewModel
import com.dust.exweather.R
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.utils.Settings

class GeneralSettingsViewModel(private val sharedPreferencesManager: SharedPreferencesManager) :
    ViewModel() {

    fun setThemeSettings(settings: Settings) {
        sharedPreferencesManager.setThemeSettings(settings)
    }

    fun calculateLanguageText(context: Context):String{
        return if (sharedPreferencesManager.getLanguageSettings() == Settings.LANGUAGE_PERSIAN) context.getString(
            R.string.persian
        ) else context.getString(R.string.english)
    }

    fun calculateNightModeCheckedButton():Boolean{
        return sharedPreferencesManager.getThemeSettings() == Settings.THEME_DARK
    }

    fun calculateNotificationText(context: Context):String{
        return if (sharedPreferencesManager.getNotificationSettings() == Settings.NOTIFICATION_ON) context.getString(
            R.string.enable
        ) else context.getString(R.string.disable)
    }

    fun getCurrentLanguageSetting():Settings{
        return sharedPreferencesManager.getLanguageSettings()
    }

    fun setLanguageSettings(settings: Settings){
        sharedPreferencesManager.setLanguageSettings(settings)
    }

    fun getNotificationSettings():Settings{
        return sharedPreferencesManager.getNotificationSettings()
    }

    fun setNotificationSettings(settings: Settings){
        sharedPreferencesManager.setNotificationSettings(settings)
    }

}