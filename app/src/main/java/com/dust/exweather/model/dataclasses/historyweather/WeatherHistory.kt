package com.dust.exweather.model.dataclasses.historyweather

import com.dust.exweather.model.dataclasses.currentweather.main.Location

data class WeatherHistory(
    val forecast: Forecast,
    val location: Location
)