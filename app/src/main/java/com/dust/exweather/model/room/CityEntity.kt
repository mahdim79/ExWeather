package com.dust.exweather.model.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_table")
data class CityEntity(@PrimaryKey val id:Int , var cityName:String)
