package com.dust.exweather.utils

import android.content.Context
import com.dust.exweather.R

fun String.convertAmPm(context: Context): String =
    this.replace("AM", context.getString(R.string.morning))
        .replace("PM", context.getString(R.string.afterNoon))