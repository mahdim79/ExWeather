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

    fun getSplashScreenImageAnimation(): AnimationSet {
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
        val rotateAnimation = RotateAnimation(
            0f,
            360f,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f
        )
        rotateAnimation.apply {
            duration = 1000
            fillAfter = true
            interpolator = AccelerateDecelerateInterpolator()
        }
        val animationSet = AnimationSet(false)
        animationSet.apply {
            addAnimation(scaleAnimation)
            addAnimation(rotateAnimation)
        }
        return animationSet
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