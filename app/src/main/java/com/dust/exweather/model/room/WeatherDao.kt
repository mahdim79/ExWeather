package com.dust.exweather.model.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherData(weatherEntity: WeatherEntity):Long

    @Query("SELECT * FROM weather_table")
    suspend fun getWeatherData():List<WeatherEntity>
}