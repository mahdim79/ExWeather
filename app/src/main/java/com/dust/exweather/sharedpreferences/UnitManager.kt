package com.dust.exweather.sharedpreferences

import com.dust.exweather.utils.Constants

class UnitManager(val sharedPreferencesManager: SharedPreferencesManager) {

    fun getPrecipitationUnit(mmAmount: String, inAmount: String): String {
        return when (sharedPreferencesManager.getWeatherUnit(Constants.PRECIPITATION_UNIT)) {
            Constants.MM -> "${mmAmount}mm"
            else -> "${inAmount}in"
        }
    }

    fun getTemperatureUnit(cPercentage: String, fpercentage: String): String {
        return when (sharedPreferencesManager.getWeatherUnit(Constants.TEMPERATURE_UNIT)) {
            Constants.C_PERCENTAGE -> "${cPercentage}°C"
            else -> "${fpercentage}°F"
        }
    }

    fun getVisibilityUnit(km: String, mile: String): String {
        return when (sharedPreferencesManager.getWeatherUnit(Constants.VISIBILITY_UNIT)) {
            Constants.KM -> "${km}Km"
            else -> "${mile}Mile"
        }
    }

    fun getWindSpeedUnit(kph: String, mph: String): String {
        return when (sharedPreferencesManager.getWeatherUnit(Constants.WIND_SPEED_UNIT)) {
            Constants.KPH -> "${kph}Kph"
            else -> "${mph}Mph"
        }
    }

    fun getPressureUnit(inAmount: String, mbAmount: String): String {
        return when (sharedPreferencesManager.getWeatherUnit(Constants.PRESSURE_UNIT)) {
            Constants.IN -> "${inAmount}Pa/In"
            else -> "${inAmount}Mb"
        }
    }
}