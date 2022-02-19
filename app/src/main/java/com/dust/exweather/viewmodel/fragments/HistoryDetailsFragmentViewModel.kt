package com.dust.exweather.viewmodel.fragments

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.historyweather.Forecastday
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.utils.UtilityFunctions
import com.dust.exweather.utils.convertAmPm

class HistoryDetailsFragmentViewModel(private val repository: CurrentWeatherRepository) :
    ViewModel() {
    fun getWeatherLiveDataFromCache(): LiveData<List<WeatherEntity>> =
        repository.getLiveWeatherDataFromCache()

    fun calculateHistoryWeatherDetailsData(context: Context, forecastDay:Forecastday):String{
        val stringBuilder = StringBuilder()
        context.apply {
            stringBuilder.apply {
                append(
                    getString(
                        R.string.sunrise,
                        forecastDay.astro.sunrise.convertAmPm(context)
                    ).plus("\n")
                )
                append(
                    getString(
                        R.string.sunset,
                        forecastDay.astro.sunset.convertAmPm(context)
                    ).plus("\n")
                )
                append(
                    getString(
                        R.string.moonrise,
                        forecastDay.astro.moonrise.convertAmPm(context)
                    ).plus("\n")
                )
                append(
                    getString(
                        R.string.moonset,
                        forecastDay.astro.moonset.convertAmPm(context)
                    ).plus("\n")
                )
                append(
                    getString(
                        R.string.moonIllumination,
                        forecastDay.astro.moon_illumination.convertAmPm(context)
                    ).plus("\n")
                )
                append(
                    getString(R.string.moonPhase, forecastDay.astro.moon_phase.convertAmPm(context)).plus(
                        "\n"
                    )
                )
                append(getString(R.string.uvIndex, forecastDay.day.uv.toString()))
            }
        }
        return stringBuilder.toString()
    }

    fun calculateCurrentData(map: List<MainWeatherData>, latLong:String?, date:String?): Forecastday? {
        map.forEach { mainWeatherData ->
            if (UtilityFunctions.createLatLongPattern(mainWeatherData.historyDetailsData!!.location) == latLong
            )
                mainWeatherData.historyDetailsData?.let { weatherHistory ->
                    weatherHistory.forecast.forecastday.forEach { forecastDay ->
                        if (date == forecastDay.date)
                            return forecastDay
                    }
                }
        }
        return null
    }
}