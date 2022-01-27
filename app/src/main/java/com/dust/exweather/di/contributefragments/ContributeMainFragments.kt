package com.dust.exweather.di.contributefragments

import com.dust.exweather.ui.fragments.others.WeatherDetailsFragment
import com.dust.exweather.ui.fragments.aboutfragments.AboutUsFragment
import com.dust.exweather.ui.fragments.others.ForecastDetailsFragment
import com.dust.exweather.ui.fragments.settingfragments.GeneralSettingsFragment
import com.dust.exweather.ui.fragments.settingfragments.WeatherSettingsFragment
import com.dust.exweather.ui.fragments.weatherfragments.CurrentWeatherFragment
import com.dust.exweather.ui.fragments.weatherfragments.WeatherHistoryFragment
import com.dust.exweather.ui.fragments.weatherfragments.WeatherForecastFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ContributeMainFragments {
    @ContributesAndroidInjector
    abstract fun contributeCurrentWeatherFragment(): CurrentWeatherFragment

    @ContributesAndroidInjector
    abstract fun contributeHistoryWeatherFragment(): WeatherHistoryFragment

    @ContributesAndroidInjector
    abstract fun contributePredictionWeatherFragment(): WeatherForecastFragment

    @ContributesAndroidInjector
    abstract fun contributeGeneralSettingsFragment() :GeneralSettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeWeatherSettingsFragment():WeatherSettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeAboutUsFragment():AboutUsFragment

    @ContributesAndroidInjector
    abstract fun contributeWeatherDetailsFragment(): WeatherDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeForecastDetailsFragment():ForecastDetailsFragment

}