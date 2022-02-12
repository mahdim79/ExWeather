package com.dust.exweather.model.dataclasses.location

data class LocationData(val locationName: String, val latlong: String, var default: Boolean, var redRemoveState:Boolean = false)