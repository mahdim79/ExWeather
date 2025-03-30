package com.dust.exweather.ui.anim

import android.view.animation.*
import javax.inject.Inject

class AnimationFactory @Inject constructor() {

    fun getAlphaAnimation(form: Float, to: Float, duration: Long): AlphaAnimation {
        val alphaAnimation = AlphaAnimation(form, to)
        alphaAnimation.duration = duration
        alphaAnimation.interpolator = AccelerateDecelerateInterpolator()
        alphaAnimation.fillAfter = true
        return alphaAnimation
    }

    fun getSplashScreenImageAnimation(): ScaleAnimation {
        val scaleAnimation = ScaleAnimation(
            1.5f,
            1f,
            1.5f,
            1f,
            ScaleAnimation.RELATIVE_TO_SELF,
            0.5f,
            ScaleAnimation.RELATIVE_TO_SELF,
            0.5f
        )

        scaleAnimation.apply {
            duration = 1000
            interpolator = OvershootInterpolator(2f)
        }
        return scaleAnimation
    }

    fun getMainScaleAnimation(): ScaleAnimation {
        ScaleAnimation(
            0f,
            1f,
            0f,
            1f,
            ScaleAnimation.RELATIVE_TO_SELF,
            0.5f,
            ScaleAnimation.RELATIVE_TO_SELF,
            0.5f
        ).apply {
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
            fillAfter = true
            return this
        }
    }

}