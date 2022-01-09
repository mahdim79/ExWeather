package com.dust.exweather.model.dataclasses.maindataclass

import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasses.forecastweather.WeatherForecast
import com.dust.exweather.model.dataclasses.historyweather.WeatherHistory

data class MainWeatherData(
    var current: CurrentData?,
    var forecastData: WeatherForecast?,
    var historyData: WeatherHistory?
)
