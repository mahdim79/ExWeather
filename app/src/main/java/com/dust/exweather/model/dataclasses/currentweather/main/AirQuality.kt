package com.dust.exweather.model.dataclasses.currentweather.main

data class AirQuality(
    val co: Double?,
    val `gb-defra-index`: Int?,
    val no2: Double?,
    val o3: Double?,
    val pm10: Double?,
    val pm2_5: Double?,
    val so2: Double?,
    val `us-epa-index`: Int?

    // TODO: 1/27/2022 create a pie chart from following amounts in currentDetailsFragment
)