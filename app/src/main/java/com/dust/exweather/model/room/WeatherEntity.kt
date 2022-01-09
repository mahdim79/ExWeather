package com.dust.exweather.model.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_table")
data class WeatherEntity(@PrimaryKey(autoGenerate = false) var id:Int = 0, var data:String?)