package com.dust.exweather.model.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CityEntity::class , WeatherEntity::class] , version = 1)
abstract class RoomManager:RoomDatabase() {
    abstract fun getCityDao():CityDao
    abstract fun getCurrentWeatherDao():WeatherDao
}