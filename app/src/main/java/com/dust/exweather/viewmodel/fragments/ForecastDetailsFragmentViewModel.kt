package com.dust.exweather.viewmodel.fragments

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.forecastweather.Forecastday
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.utils.convertAmPm

class ForecastDetailsFragmentViewModel(
    private val currentWeatherRepository: CurrentWeatherRepository
) : ViewModel() {

    fun getWeatherDataFromApi(context: Context) {
        currentWeatherRepository.doApiCall(context)
    }

    fun getWeatherApiCallStateLiveData(): LiveData<DataWrapper<String>> =
        currentWeatherRepository.getWeatherApiCallStateLiveData()

    fun getLiveWeatherDataFromCache(): LiveData<List<WeatherEntity>> =
        currentWeatherRepository.getLiveWeatherDataFromCache()

    fun calculateForecastWeatherDetailsData(context: Context, forecastDay: Forecastday): String {
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
                    getString(
                        R.string.moonPhase,
                        forecastDay.astro.moon_phase.convertAmPm(context)
                    ).plus(
                        "\n"
                    )
                )
                append(getString(R.string.uvIndex, forecastDay.day.uv.toString()))
            }
        }
        return stringBuilder.toString()
    }

    fun calculateCurrentData(
        listData: List<MainWeatherData>,
        location: String?,
        date: String?
    ): MainWeatherData? {
        if (!location.isNullOrEmpty() && !date.isNullOrEmpty()) {
            listData.forEach { mainWeatherData ->
                if (mainWeatherData.location == location)
                    return mainWeatherData
            }
        }
        return null
    }

    fun calculateData(data: MainWeatherData, date: String?): Forecastday? {
        data.forecastDetailsData?.let { weatherForecast ->
            weatherForecast.forecast.forecastday.forEach { forecastDay ->
                if (forecastDay.date == date)
                    return forecastDay
            }
        }
        return null
    }
}