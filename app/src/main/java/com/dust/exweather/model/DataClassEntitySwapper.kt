package com.dust.exweather.model

import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.room.CurrentWeatherEntity
import com.google.gson.Gson

fun CurrentData.toEntity():CurrentWeatherEntity{
    return CurrentWeatherEntity(data = Gson().toJson(this))
}

fun CurrentWeatherEntity.toDataClass():CurrentData{
    return Gson().fromJson(this.data , CurrentData::class.java)
}