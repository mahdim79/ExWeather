package com.dust.exweather.model.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherData(weatherEntities: List<WeatherEntity>)

    @Delete
    suspend fun removeWeatherData(weatherEntity: WeatherEntity)

    @Query("SELECT * FROM weather_table")
    suspend fun getDirectWeatherData(): List<WeatherEntity>

    @Query("SELECT * FROM weather_table")
    fun getLiveWeatherData(): LiveData<List<WeatherEntity>>
}