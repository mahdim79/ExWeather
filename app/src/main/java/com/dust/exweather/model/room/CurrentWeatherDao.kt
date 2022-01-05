package com.dust.exweather.model.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CurrentWeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentWeather(currentWeatherEntity: CurrentWeatherEntity):Long

    @Query("SELECT * FROM current_weather_table")
    fun getCurrentWeatherData():LiveData<List<CurrentWeatherEntity>>
}