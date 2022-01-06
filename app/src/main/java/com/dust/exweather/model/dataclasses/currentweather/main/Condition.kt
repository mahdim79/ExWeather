package com.dust.exweather.model.dataclasses.currentweather.main

data class Condition(
    val code: Int,
    val icon: String,
    val text: String,
    val weatherPersianText:String
)