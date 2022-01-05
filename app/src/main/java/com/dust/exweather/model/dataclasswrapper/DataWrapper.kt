package com.dust.exweather.model.dataclasswrapper

import com.dust.exweather.utils.DataStatus

data class DataWrapper<T>(var data:T? , var status:DataStatus)