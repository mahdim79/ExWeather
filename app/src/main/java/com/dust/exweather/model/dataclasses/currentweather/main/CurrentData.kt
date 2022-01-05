package com.dust.exweather.model.dataclasses.currentweather.main

import com.dust.exweather.model.dataclasses.currentweather.other.Current
import com.dust.exweather.model.dataclasses.currentweather.other.Location

data class CurrentData(
    val current: Current?,
    val location: Location?,
    val error: String
)