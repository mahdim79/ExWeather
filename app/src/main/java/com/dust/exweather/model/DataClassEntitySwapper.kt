package com.dust.exweather.model

import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.room.WeatherEntity
import com.google.gson.Gson

fun MainWeatherData.toEntity(): WeatherEntity {
    return WeatherEntity(data = Gson().toJson(this), id = this.id)
}

fun WeatherEntity.toDataClass(): MainWeatherData {
    val data = Gson().fromJson(this.data, MainWeatherData::class.java)
    data.id = this.id!!
    return data
}