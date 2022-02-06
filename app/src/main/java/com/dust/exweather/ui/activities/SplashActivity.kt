package com.dust.exweather.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.*
import com.dust.exweather.MyApplication
import com.dust.exweather.R
import com.dust.exweather.service.NotificationService
import com.dust.exweather.utils.UtilityFunctions
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setCurrentTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setUpImageAnimation()
        setUpTextAnimation()
        setUpTranslationHandler()
        startNotificationService()
    }

    private fun startNotificationService() {
        if (!UtilityFunctions.checkNotificationServiceRunning(applicationContext))
            startService(Intent(this, NotificationService::class.java))
    }

    private fun setCurrentTheme() {
        setTheme((applicationContext as MyApplication).getCurrentThemeResId())
    }

    private fun setUpTranslationHandler() {
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 4000)
    }

    private fun setUpTextAnimation() {
        val alphaAnimation = AlphaAnimation(0f, 1f)
        alphaAnimation.apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            fillAfter = true
        }
        splashText.startAnimation(alphaAnimation)
    }

    private fun setUpImageAnimation() {
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
            3600f,
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

        splashImage.startAnimation(animationSet)
    }
}