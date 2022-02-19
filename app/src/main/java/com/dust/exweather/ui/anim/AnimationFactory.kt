package com.dust.exweather.ui.anim

import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import javax.inject.Inject

class AnimationFactory @Inject constructor() {

    fun getAlphaAnimation(form:Float, to:Float, duration:Long): AlphaAnimation {
        val alphaAnimation = AlphaAnimation(form, to)
        alphaAnimation.duration = duration
        alphaAnimation.interpolator = AccelerateDecelerateInterpolator()
        alphaAnimation.fillAfter = true
        return alphaAnimation
    }
}