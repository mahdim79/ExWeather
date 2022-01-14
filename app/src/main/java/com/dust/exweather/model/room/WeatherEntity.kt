package com.dust.exweather.model.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_table")
data class WeatherEntity(@PrimaryKey(autoGenerate = true) var id:Int? = null, var data:String?)