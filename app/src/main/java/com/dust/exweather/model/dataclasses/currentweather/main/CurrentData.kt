package com.dust.exweather.model.dataclasses.currentweather.main

data class CurrentData(
    val current: Current?,
    val location: Location?,
    val error: String
)