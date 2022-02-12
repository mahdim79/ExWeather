package com.dust.exweather.sharedpreferences

import android.content.Context
import com.dust.exweather.utils.Constants
import com.dust.exweather.utils.Settings

class SharedPreferencesManager(private val context: Context) {
    val pref = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE)

    fun setLanguageSettings(setting: Settings) {
        pref.edit().putInt(
            Constants.LANGUAGE_KEY,
            if (setting == Settings.LANGUAGE_PERSIAN) Constants.LANGUAGE_PERSIAN else Constants.LANGUAGE_ENGLISH
        ).apply()
    }

    fun getLanguageSettings(): Settings {
        return if (pref.getInt(
                Constants.LANGUAGE_KEY,
                Constants.LANGUAGE_PERSIAN
            ) == Constants.LANGUAGE_PERSIAN
        )
            Settings.LANGUAGE_PERSIAN
        else
            Settings.LANGUAGE_ENGLISH
    }

    fun setThemeSettings(setting: Settings) {
        pref.edit().putInt(
            Constants.THEME_KEY,
            if (setting == Settings.THEME_LIGHT) Constants.THEME_LIGHT else Constants.THEME_DARK
        ).apply()
    }

    fun getThemeSettings(): Settings {
        return if (pref.getInt(
                Constants.THEME_KEY,
                Constants.THEME_LIGHT
            ) == Constants.THEME_LIGHT
        )
            Settings.THEME_LIGHT
        else
            Settings.THEME_DARK
    }

    fun setNotificationSettings(setting: Settings) {
        pref.edit().putInt(
            Constants.NOTIFICATION_KEY,
            if (setting == Settings.NOTIFICATION_ON) Constants.NOTIFICATION_ON else Constants.NOTIFICATION_OFF
        ).apply()
    }

    fun getNotificationSettings(): Settings {
        return if (pref.getInt(
                Constants.NOTIFICATION_KEY,
                Constants.NOTIFICATION_ON
            ) == Constants.NOTIFICATION_ON
        )
            Settings.NOTIFICATION_ON
        else
            Settings.NOTIFICATION_OFF
    }

    fun setLastNotificationTimeEpoch(timeEpoch: Long) {
        pref.edit().putLong(Constants.NOTIFICATION_TIME_EPOCH_KEY, timeEpoch).apply()
    }

    fun getLastNotificationTimeEpoch(): Long =
        pref.getLong(Constants.NOTIFICATION_TIME_EPOCH_KEY, 0L)

    fun getDefaultLocation(): String? = pref.getString(Constants.DEFAULT_LOCATION_KEY, "")

    fun setDefaultLocation(location: String) {
        pref.edit().putString(Constants.DEFAULT_LOCATION_KEY, location).apply()
    }

}