package com.dust.exweather.di.contributeactivities

import android.app.Application
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.Module
import dagger.Provides

@Module
class MainActivityModule {
    @Provides
    fun provideAlphaAnimation():AlphaAnimation{
        val alphaAnimation = AlphaAnimation(0f, 1f)
        alphaAnimation.duration = 800
        alphaAnimation.interpolator = AccelerateDecelerateInterpolator()
        alphaAnimation.fillAfter = true
        return alphaAnimation
    }

}