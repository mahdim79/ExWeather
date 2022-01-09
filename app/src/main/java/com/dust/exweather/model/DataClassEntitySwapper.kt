package com.dust.exweather.model

import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.room.WeatherEntity
import com.google.gson.Gson

fun MainWeatherData.toEntity():WeatherEntity{
    return WeatherEntity(data = Gson().toJson(this))
}

fun WeatherEntity.toDataClass():MainWeatherData{
    return Gson().fromJson(this.data , MainWeatherData::class.java)
}