package com.dust.exweather.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.dust.exweather.MyApplication
import com.dust.exweather.R
import com.dust.exweather.service.NotificationService
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.utils.Constants
import com.dust.exweather.utils.Settings
import com.dust.exweather.utils.customviews.CTextView
import com.koushikdutta.ion.Ion
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var toolbar: Toolbar
    private lateinit var titleText: TextView

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        setCurrentTheme()
        setCurrentLocaleConfiguration()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // primary ui stuff such as: navigationComponent, toolbar, statusBar etc...
        setUpPrimaryUiStuff()
    }

    private fun setCurrentLocaleConfiguration() {
        val localeStr = (applicationContext as MyApplication).getCurrentLocaleStr()
        val locale = Locale(localeStr)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun setCurrentTheme() {
        setTheme((applicationContext as MyApplication).getCurrentThemeResId())
    }

    private fun setUpPrimaryUiStuff() {
        setUpViews()
        setUpNaVController()
        setUpActionBar()
        setUpStatusBar()
        setUpNavigationComponent()
        setUpNavigationView()

    }

    private fun setUpViews() {
        toolbar = findViewById(R.id.mainToolbar)
        titleText = findViewById(R.id.toolbarTitle)
    }

    private fun setUpStatusBar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    private fun setUpNaVController() {
        navController = findNavController(R.id.mainFragmentContainerView)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.currentWeatherFragment -> {
                    mainNavView.setCheckedItem(destination.id)
                    setBottomNavigationVisibility(true)
                    titleText.text = getString(R.string.currentWeather)
                }
                R.id.weatherHistoryFragment -> {
                    mainNavView.setCheckedItem(destination.id)
                    setBottomNavigationVisibility(true)
                    titleText.text = getString(R.string.weatherHistory)
                }
                R.id.weatherPredictionFragment -> {
                    mainNavView.setCheckedItem(destination.id)
                    setBottomNavigationVisibility(true)
                    titleText.text = getString(R.string.weatherPrediction)
                }
                R.id.generalSettingsFragment -> {
                    mainNavView.setCheckedItem(destination.id)
                    setBottomNavigationVisibility(false)
                    titleText.text = getString(R.string.generalSettings)
                }
                R.id.weatherSettingsFragment -> {
                    mainNavView.setCheckedItem(destination.id)
                    setBottomNavigationVisibility(false)
                    titleText.text = getString(R.string.weatherSettings)
                }
                R.id.aboutUsFragment -> {
                    mainNavView.setCheckedItem(destination.id)
                    setBottomNavigationVisibility(false)
                    titleText.text = getString(R.string.aboutUs)
                }
                R.id.weatherDetailsFragment -> {
                    setBottomNavigationVisibility(false)
                    titleText.text = getString(R.string.weatherDetails)
                }
                R.id.addLocationFragment -> {
                    setBottomNavigationVisibility(false)
                    titleText.text = getString(R.string.addNewLocation)
                }
            }
        }
    }

    private fun setUpNavigationView() {
        mainNavView.bringToFront()
        setUpNavigationViewHeader()
    }

    private fun setUpNavigationViewHeader() {
        val headerLayout = mainNavView.getHeaderView(0)
        headerLayout.apply {
            val headerImageView = findViewById<ImageView>(R.id.navigationViewHeaderImage)
            Ion.with(context)
                .load("http://www.montgomeryruritanclub.com/Site/images/annimated/lg.rainy-preloader.gif")
                .intoImageView(headerImageView)
        }
    }

    private fun setBottomNavigationVisibility(b: Boolean) {
        mainBottomNavigation.visibility = if (b) View.VISIBLE else View.GONE
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar)
        title = null

    }

    private fun setUpNavigationComponent() {
        NavigationUI.setupActionBarWithNavController(
            this,
            navController,
            mainDrawerLayout
        )
        NavigationUI.setupWithNavController(
            mainNavView,
            navController
        )
        NavigationUI.setupWithNavController(mainBottomNavigation, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, mainDrawerLayout)
    }
}