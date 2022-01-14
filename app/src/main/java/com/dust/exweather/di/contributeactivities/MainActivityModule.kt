package com.dust.exweather.di.contributeactivities

import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
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