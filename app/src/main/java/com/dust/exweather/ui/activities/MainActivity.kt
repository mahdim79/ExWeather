package com.dust.exweather.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.dust.exweather.MyApplication
import com.dust.exweather.R
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.utils.Settings
import com.dust.exweather.utils.customviews.CTextView
import com.dust.exweather.viewmodel.activities.MainActivityViewModel
import com.google.android.material.card.MaterialCardView
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.appbarLayout
import kotlinx.android.synthetic.main.activity_main.cl_main_navigationMenuRoot
import kotlinx.android.synthetic.main.activity_main.cv_main_aboutUs
import kotlinx.android.synthetic.main.activity_main.cv_main_currentWeather
import kotlinx.android.synthetic.main.activity_main.cv_main_generalSettings
import kotlinx.android.synthetic.main.activity_main.cv_main_weatherForecast
import kotlinx.android.synthetic.main.activity_main.cv_main_weatherHistory
import kotlinx.android.synthetic.main.activity_main.cv_main_weatherSettings
import kotlinx.android.synthetic.main.activity_main.iv_navigationMenu_aboutUs
import kotlinx.android.synthetic.main.activity_main.iv_navigationMenu_currentWeather
import kotlinx.android.synthetic.main.activity_main.iv_navigationMenu_generalSettings
import kotlinx.android.synthetic.main.activity_main.iv_navigationMenu_weatherForecast
import kotlinx.android.synthetic.main.activity_main.iv_navigationMenu_weatherHistory
import kotlinx.android.synthetic.main.activity_main.iv_navigationMenu_weatherSettings
import kotlinx.android.synthetic.main.activity_main.ll_main_darkTheme
import kotlinx.android.synthetic.main.activity_main.ll_main_lightTheme
import kotlinx.android.synthetic.main.activity_main.mainBottomNavigation
import kotlinx.android.synthetic.main.activity_main.mainDrawerLayout
import kotlinx.android.synthetic.main.activity_main.tv_navigationMenu_aboutUs
import kotlinx.android.synthetic.main.activity_main.tv_navigationMenu_currentWeather
import kotlinx.android.synthetic.main.activity_main.tv_navigationMenu_generalSettings
import kotlinx.android.synthetic.main.activity_main.tv_navigationMenu_weatherForecast
import kotlinx.android.synthetic.main.activity_main.tv_navigationMenu_weatherHistory
import kotlinx.android.synthetic.main.activity_main.tv_navigationMenu_weatherSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var toolbar: Toolbar
    private lateinit var titleText: TextView
    private lateinit var addLocationImageView: ImageView

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){}

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setupViewModel()
        setTheme(viewModel.getCurrentTheme(applicationContext))
        setCurrentLocaleConfiguration()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // primary ui stuff such as: navigationComponent, toolbar, statusBar etc...
        setUpPrimaryUiStuff()
        viewModel.startNotificationService(applicationContext)
        checkNotificationPermission()

    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
    }

    private fun setCurrentLocaleConfiguration() {
        val localeStr = (applicationContext as MyApplication).getCurrentLocaleStr()
        val locale = Locale(localeStr)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun setUpPrimaryUiStuff() {
        setUpViews()
        setUpNaVController()
        setUpAddLocationButton()
        setUpActionBar()
        setUpStatusBar()
        setUpNavigationComponent()
        setUpNavigationView()
        setUpThemeSelector()
    }

    private fun setUpThemeSelector() {
        if (sharedPreferencesManager.getThemeSettings() == Settings.THEME_DARK) {
            ll_main_lightTheme.background = null
            ll_main_darkTheme.background =
                ResourcesCompat.getDrawable(resources, R.drawable.theme_shape_background, theme)
        }

        ll_main_lightTheme.setOnClickListener {
            sharedPreferencesManager.setThemeSettings(Settings.THEME_LIGHT)
            restartApplication()
        }

        ll_main_darkTheme.setOnClickListener {
            sharedPreferencesManager.setThemeSettings(Settings.THEME_DARK)
            restartApplication()
        }
    }

    private fun setUpAddLocationButton() {
        addLocationImageView.setOnClickListener {
            navController.navigate(R.id.weatherSettingsFragment)
        }
    }

    private fun restartApplication() {
        finishAffinity()
        startActivity(Intent(this, SplashActivity::class.java))
    }

    private fun setUpViews() {
        toolbar = findViewById(R.id.mainToolbar)
        titleText = findViewById(R.id.toolbarTitle)
        addLocationImageView = findViewById(R.id.addLocationImageView)
    }

    private fun setUpStatusBar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    private fun selectNavigationMenuItem(
        layoutResId: Int,
        textViewResId: Int,
        imageViewResId: Int
    ) {
        unSelectAllNavigationMenuItems()
        val itemCard = findViewById<MaterialCardView>(layoutResId)
        val textView = findViewById<CTextView>(textViewResId)
        val imageView = findViewById<ImageView>(imageViewResId)
        itemCard.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue2))
        textView.setTextColor(Color.WHITE)
        imageView.imageTintList = ColorStateList.valueOf(Color.WHITE)
    }

    private fun getColorPrimary(): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true)
        return typedValue.data
    }

    private fun unSelectAllNavigationMenuItems() {
        val colorPrimary = getColorPrimary()
        cv_main_currentWeather.backgroundTintList = ColorStateList.valueOf(colorPrimary)
        cv_main_weatherForecast.backgroundTintList = ColorStateList.valueOf(colorPrimary)
        cv_main_weatherHistory.backgroundTintList = ColorStateList.valueOf(colorPrimary)
        cv_main_generalSettings.backgroundTintList = ColorStateList.valueOf(colorPrimary)
        cv_main_weatherSettings.backgroundTintList = ColorStateList.valueOf(colorPrimary)
        cv_main_aboutUs.backgroundTintList = ColorStateList.valueOf(colorPrimary)

        val grayColor = ContextCompat.getColor(this, R.color.gray)

        tv_navigationMenu_currentWeather.setTextColor(grayColor)
        iv_navigationMenu_currentWeather.imageTintList = ColorStateList.valueOf(grayColor)

        tv_navigationMenu_weatherForecast.setTextColor(grayColor)
        iv_navigationMenu_weatherForecast.imageTintList = ColorStateList.valueOf(grayColor)

        tv_navigationMenu_weatherHistory.setTextColor(grayColor)
        iv_navigationMenu_weatherHistory.imageTintList = ColorStateList.valueOf(grayColor)

        tv_navigationMenu_generalSettings.setTextColor(grayColor)
        iv_navigationMenu_generalSettings.imageTintList = ColorStateList.valueOf(grayColor)

        tv_navigationMenu_weatherSettings.setTextColor(grayColor)
        iv_navigationMenu_weatherSettings.imageTintList = ColorStateList.valueOf(grayColor)

        tv_navigationMenu_aboutUs.setTextColor(grayColor)
        iv_navigationMenu_aboutUs.imageTintList = ColorStateList.valueOf(grayColor)
    }

    private fun setUpNaVController() {
        navController = findNavController(R.id.mainFragmentContainerView)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            appbarLayout.setExpanded(true, true)
            addLocationImageView.visibility = View.VISIBLE
            when (destination.id) {
                R.id.currentWeatherFragment -> {
                    selectNavigationMenuItem(
                        R.id.cv_main_currentWeather,
                        R.id.tv_navigationMenu_currentWeather,
                        R.id.iv_navigationMenu_currentWeather
                    )
                    setBottomNavigationVisibility(true)
                    titleText.text = getString(R.string.currentWeather)
                }

                R.id.weatherHistoryFragment -> {
                    selectNavigationMenuItem(
                        R.id.cv_main_weatherHistory,
                        R.id.tv_navigationMenu_weatherHistory,
                        R.id.iv_navigationMenu_weatherHistory
                    )
                    setBottomNavigationVisibility(true)
                    titleText.text = getString(R.string.weatherHistory)
                }

                R.id.weatherPredictionFragment -> {
                    selectNavigationMenuItem(
                        R.id.cv_main_weatherForecast,
                        R.id.tv_navigationMenu_weatherForecast,
                        R.id.iv_navigationMenu_weatherForecast
                    )
                    setBottomNavigationVisibility(true)
                    titleText.text = getString(R.string.weatherPrediction)
                }

                R.id.generalSettingsFragment -> {
                    selectNavigationMenuItem(
                        R.id.cv_main_generalSettings,
                        R.id.tv_navigationMenu_generalSettings,
                        R.id.iv_navigationMenu_generalSettings
                    )
                    setBottomNavigationVisibility(false)
                    titleText.text = getString(R.string.generalSettings)
                    addLocationImageView.visibility = View.GONE
                }

                R.id.weatherSettingsFragment -> {
                    selectNavigationMenuItem(
                        R.id.cv_main_weatherSettings,
                        R.id.tv_navigationMenu_weatherSettings,
                        R.id.iv_navigationMenu_weatherSettings
                    )
                    setBottomNavigationVisibility(false)
                    titleText.text = getString(R.string.weatherSettings)
                    addLocationImageView.visibility = View.GONE
                }

                R.id.aboutUsFragment -> {
                    selectNavigationMenuItem(
                        R.id.cv_main_aboutUs,
                        R.id.tv_navigationMenu_aboutUs,
                        R.id.iv_navigationMenu_aboutUs
                    )
                    setBottomNavigationVisibility(false)
                    titleText.text = getString(R.string.aboutUs)
                    addLocationImageView.visibility = View.GONE
                }

                R.id.weatherDetailsFragment -> {
                    setBottomNavigationVisibility(true)
                    titleText.text = getString(R.string.weatherDetails)
                    addLocationImageView.visibility = View.GONE
                }

                R.id.addLocationFragment -> {
                    setBottomNavigationVisibility(false)
                    titleText.text = getString(R.string.addNewLocation)
                    addLocationImageView.visibility = View.GONE
                }

                R.id.historyDetailsFragment -> {
                    setBottomNavigationVisibility(true)
                    titleText.text = getString(R.string.weatherHistory)
                    addLocationImageView.visibility = View.GONE
                }

                R.id.forecastDetailsFragment -> {
                    setBottomNavigationVisibility(true)
                    titleText.text = getString(R.string.weatherPrediction)
                    addLocationImageView.visibility = View.GONE
                }
            }
        }
    }

    private fun navigateFromNavigationMenu(fragmentId: Int) {
        if (navController.currentDestination?.id == fragmentId)
            return
        lifecycleScope.launch {
            delay(300)
            navController.navigate(fragmentId)
        }
    }

    private fun setUpNavigationView() {
        cl_main_navigationMenuRoot.bringToFront()

        cv_main_currentWeather.setOnClickListener {
            selectNavigationMenuItem(
                R.id.cv_main_currentWeather,
                R.id.tv_navigationMenu_currentWeather,
                R.id.iv_navigationMenu_currentWeather
            )
            mainDrawerLayout.closeDrawers()
            navigateFromNavigationMenu(R.id.currentWeatherFragment)
        }

        cv_main_weatherForecast.setOnClickListener {
            selectNavigationMenuItem(
                R.id.cv_main_weatherForecast,
                R.id.tv_navigationMenu_weatherForecast,
                R.id.iv_navigationMenu_weatherForecast
            )
            mainDrawerLayout.closeDrawers()
            navigateFromNavigationMenu(R.id.weatherPredictionFragment)
        }

        cv_main_weatherHistory.setOnClickListener {
            selectNavigationMenuItem(
                R.id.cv_main_weatherHistory,
                R.id.tv_navigationMenu_weatherHistory,
                R.id.iv_navigationMenu_weatherHistory
            )
            mainDrawerLayout.closeDrawers()
            navigateFromNavigationMenu(R.id.weatherHistoryFragment)
        }

        cv_main_generalSettings.setOnClickListener {
            selectNavigationMenuItem(
                R.id.cv_main_generalSettings,
                R.id.tv_navigationMenu_generalSettings,
                R.id.iv_navigationMenu_generalSettings
            )
            mainDrawerLayout.closeDrawers()
            navigateFromNavigationMenu(R.id.generalSettingsFragment)
        }

        cv_main_weatherSettings.setOnClickListener {
            selectNavigationMenuItem(
                R.id.cv_main_weatherSettings,
                R.id.tv_navigationMenu_weatherSettings,
                R.id.iv_navigationMenu_weatherSettings
            )
            mainDrawerLayout.closeDrawers()
            navigateFromNavigationMenu(R.id.weatherSettingsFragment)
        }

        cv_main_aboutUs.setOnClickListener {
            selectNavigationMenuItem(
                R.id.cv_main_aboutUs,
                R.id.tv_navigationMenu_aboutUs,
                R.id.iv_navigationMenu_aboutUs
            )
            mainDrawerLayout.closeDrawers()
            navigateFromNavigationMenu(R.id.aboutUsFragment)
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

        NavigationUI.setupWithNavController(mainBottomNavigation, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, mainDrawerLayout)
    }
}