package com.dust.exweather.viewmodel.fragments

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.historyweather.Forecastday
import com.dust.exweather.model.dataclasses.historyweather.WeatherHistory
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.WeatherHistoryRepository
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import com.dust.exweather.sharedpreferences.UnitManager
import com.dust.exweather.utils.UtilityFunctions
import java.util.*

class HistoryFragmentViewModel(private val repository: WeatherHistoryRepository) : ViewModel() {

    private val historyDataList = arrayListOf<WeatherHistory>()

    fun getLiveWeatherDataFromCache(): LiveData<List<WeatherEntity>> =
        repository.getWeatherLiveDataFromCache()

    fun getHistoryDataList(): ArrayList<WeatherHistory> = historyDataList

    fun addHistoryDataToDataList(rawData: List<WeatherEntity>) {
        val data = rawData.map { it.toDataClass() }
        data.forEach { mainWeatherData ->
            mainWeatherData.historyDetailsData?.let { weatherHistory ->
                historyDataList.add(weatherHistory)
            }
        }
    }

    fun createShareSample(
        context: Context,
        forecastDay: Forecastday,
        locationName: String,
        unitManager: UnitManager
    ): String {
        val stringBuilder = StringBuilder()
        context.apply {
            stringBuilder.apply {
                append(
                    getString(
                        R.string.shareTitle,
                        locationName,
                        UtilityFunctions.getDayOfWeekByUnixTimeStamp(forecastDay.date_epoch,context),
                        UtilityFunctions.calculateCurrentDateByTimeEpoch(forecastDay.date_epoch)
                    ).plus("\n")
                )
                append(getString(R.string.weatherState, forecastDay.day.condition.text).plus("\n"))
                append(
                    getString(
                        R.string.precipitationTextmm,
                        unitManager.getPrecipitationUnit(
                            forecastDay.day.totalprecip_mm.toString(),
                            forecastDay.day.totalprecip_in.toString()
                        )
                    ).plus("\n")
                )
                append(
                    getString(
                        R.string.avgTemperature,
                        unitManager.getTemperatureUnit(
                            forecastDay.day.avgtemp_c.toString(),
                            forecastDay.day.avgtemp_f.toString()
                        )
                    ).plus("\n")
                )
                append(
                    getString(
                        R.string.minTemp,
                        unitManager.getTemperatureUnit(
                            forecastDay.day.mintemp_c.toString(),
                            forecastDay.day.mintemp_f.toString()
                        )
                    ).plus("\n")
                )
                append(
                    getString(
                        R.string.maxTemp,
                        unitManager.getTemperatureUnit(
                            forecastDay.day.maxtemp_c.toString(),
                            forecastDay.day.maxtemp_f.toString()
                        )
                    ).plus("\n")
                )
                append(
                    getString(
                        R.string.windSpeedkph,
                        unitManager.getWindSpeedUnit(
                            forecastDay.day.maxwind_kph.toString(),
                            forecastDay.day.maxwind_mph.toString()
                        )
                    ).plus("\n")
                )
            }
        }
        return stringBuilder.toString()
    }

    fun exportToCsvFile(
        forecastDay: Forecastday,
        location: String,
        fileName: String
    ): DataWrapper<String> =
        repository.createWeatherCsvFile(forecastDay, location, fileName)

}