package com.dust.exweather.model.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_weather_table")
data class CurrentWeatherEntity(@PrimaryKey(autoGenerate = false) var id:Int = 0 , var data:String?)