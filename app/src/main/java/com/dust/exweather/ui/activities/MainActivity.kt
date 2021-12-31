package com.dust.exweather.ui.activities

import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.dust.exweather.R
import com.dust.exweather.utils.customviews.CTextView
import com.koushikdutta.ion.Ion
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.File


class MainActivity : DaggerAppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var toolbar: Toolbar
    private lateinit var titleText:CTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
                    titleText.text = "آب و هوای فعلی"
                }
                R.id.weatherHistoryFragment -> {
                    mainNavView.setCheckedItem(destination.id)
                    setBottomNavigationVisibility(true)
                    titleText.text = "تاریخچه آب و هوا"
                }
                R.id.weatherPredictionFragment -> {
                    mainNavView.setCheckedItem(destination.id)
                    setBottomNavigationVisibility(true)
                    titleText.text = "پیش بینی آب و هوا"
                }
                R.id.generalSettingsFragment -> {
                    mainNavView.setCheckedItem(destination.id)
                    setBottomNavigationVisibility(false)
                    titleText.text = "تنظیمات عمومی"
                }
                R.id.weatherSettingsFragment -> {
                    mainNavView.setCheckedItem(destination.id)
                    setBottomNavigationVisibility(false)
                    titleText.text = "تنظیمات آب و هوا"
                }
                R.id.aboutUsFragment -> {
                    mainNavView.setCheckedItem(destination.id)
                    setBottomNavigationVisibility(false)
                    titleText.text = "درباره ما"
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
            Ion.with(context).load("http://www.montgomeryruritanclub.com/Site/images/annimated/lg.rainy-preloader.gif").intoImageView(headerImageView)
        }
    }

    private fun setBottomNavigationVisibility(b:Boolean){
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