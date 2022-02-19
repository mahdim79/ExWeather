package com.dust.exweather.viewmodel.activities

import android.content.Context
import androidx.lifecycle.ViewModel
import com.dust.exweather.MyApplication

class MainActivityViewModel : ViewModel() {

    fun getCurrentTheme(context: Context): Int {
        return (context.applicationContext as MyApplication).getCurrentThemeResId()
    }
}