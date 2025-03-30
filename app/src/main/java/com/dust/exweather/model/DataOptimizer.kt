package com.dust.exweather.model

import android.content.Context
import com.dust.exweather.model.dataclasses.forecastweather.Forecastday
import com.dust.exweather.model.dataclasses.historyweather.WeatherHistory
import com.dust.exweather.utils.UtilityFunctions
import retrofit2.Response
import javax.inject.Inject

class DataOptimizer @Inject constructor(){

    @Synchronized
    fun setUpHistoryFetchedData(
        staticData: WeatherHistory?,
        response: Response<WeatherHistory>?
    ): WeatherHistory? {

        if (response?.body() != null && response.isSuccessful) {
            val tempData = response.body()
            return if (staticData == null) {
                tempData
            } else {
                val tempArray =
                    arrayListOf<com.dust.exweather.model.dataclasses.historyweather.Forecastday>()
                tempArray.addAll(staticData.forecast.forecastday)
                tempArray.addAll(tempData!!.forecast.forecastday)
                staticData.forecast.forecastday = tempArray
                staticData
            }
        }
        return null
    }

    fun optimizeForecastData(
        forecastday: List<Forecastday>,
        currentDayOfWeek: String,
        context: Context
    ): List<Forecastday> {
        val listDays = arrayListOf<Forecastday>()
        listDays.addAll(forecastday)

        // calculate day of week
        for (i in listDays.indices)
            listDays[i].day.dayOfWeek =
                UtilityFunctions.getDayOfWeekByUnixTimeStamp(
                    listDays[i].date_epoch,
                    context
                )

        // find duplicate data and delete it from list
        if (!listDays.isNullOrEmpty())
            if (listDays[0].day.dayOfWeek == currentDayOfWeek)
                listDays.removeAt(0)
        return listDays
    }

    fun optimizeHistoryData(
        forecastday: List<com.dust.exweather.model.dataclasses.historyweather.Forecastday>,
        context: Context
    ): List<com.dust.exweather.model.dataclasses.historyweather.Forecastday> {

        // sort data by time epoch because its fetched by parallel method
        val historyTempList =
            arrayListOf<com.dust.exweather.model.dataclasses.historyweather.Forecastday>()
        historyTempList.addAll(forecastday)
        historyTempList.sortWith { p0, p1 ->
            if (p0.date_epoch > p1.date_epoch)
                -1
            else
                1
        }

        // calculate day of week
        for (i in historyTempList.indices) {
            val forecastDay =
                historyTempList[i]
            historyTempList[i].day.dayOfWeek =
                UtilityFunctions.getDayOfWeekByUnixTimeStamp(
                    forecastDay.date_epoch,
                    context
                )
        }

        return historyTempList
    }
}