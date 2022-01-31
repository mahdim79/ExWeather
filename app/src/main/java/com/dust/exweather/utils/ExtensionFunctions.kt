package com.dust.exweather.utils

fun String.convertAmPm():String = this.replace("AM", "صبح").replace("PM", "عصر")