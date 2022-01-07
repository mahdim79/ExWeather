package com.dust.exweather.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class UtilityFunctions {
    companion object {
        fun isNetworkConnectionEnabled(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        }
    }

}