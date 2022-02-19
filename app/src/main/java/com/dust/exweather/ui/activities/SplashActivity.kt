package com.dust.exweather.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import androidx.lifecycle.ViewModelProvider
import com.dust.exweather.BuildConfig
import com.dust.exweather.MyApplication
import com.dust.exweather.R
import com.dust.exweather.ui.anim.AnimationFactory
import com.dust.exweather.viewmodel.activities.SplashActivityViewModel
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_splash.*
import java.util.*
import javax.inject.Inject

class SplashActivity : DaggerAppCompatActivity() {
    private lateinit var viewModel: SplashActivityViewModel

    @Inject
    lateinit var animationFactory: AnimationFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        setupViewModel()
        setTheme(viewModel.getCurrentTheme(applicationContext))
        setCurrentLocaleConfiguration()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setUpImageAnimation()
        setUpTextAnimation()
        setUpTranslationHandler()
        viewModel.startNotificationService(applicationContext)
        setUpSplashTextView()
    }

    private fun setCurrentLocaleConfiguration() {
        val localeStr = (applicationContext as MyApplication).getCurrentLocaleStr()
        val locale = Locale(localeStr)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[SplashActivityViewModel::class.java]
    }

    private fun setUpSplashTextView() {
        splashText.text = BuildConfig.VERSION_NAME
    }

    private fun setUpTranslationHandler() {
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 4000)
    }

    private fun setUpTextAnimation() {
        splashText.startAnimation(animationFactory.getAlphaAnimation(0f,1f,1000))
    }

    private fun setUpImageAnimation() {
        splashImage.startAnimation(animationFactory.getSplashScreenImageAnimation())
    }
}