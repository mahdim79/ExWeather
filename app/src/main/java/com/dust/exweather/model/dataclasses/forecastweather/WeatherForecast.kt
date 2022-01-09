package com.dust.exweather.model.dataclasses.forecastweather

import com.dust.exweather.model.dataclasses.currentweather.main.Current
import com.dust.exweather.model.dataclasses.currentweather.main.Location

data class WeatherForecast(
    val current: Current,
    val forecast: Forecast,
    val location: Location
)