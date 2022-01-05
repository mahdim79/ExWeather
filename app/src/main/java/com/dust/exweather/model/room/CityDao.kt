package com.dust.exweather.model.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CityDao {
    @Insert
    suspend fun insertCity(cityEntity: CityEntity):Long

    @Query("SELECT * FROM city_table")
    suspend fun getCitiesNames():List<CityEntity>
}