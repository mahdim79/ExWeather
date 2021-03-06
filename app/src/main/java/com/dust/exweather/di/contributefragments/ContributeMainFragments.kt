package com.dust.exweather.di.contributefragments

import com.dust.exweather.di.contributefragments.scopes.WeatherHistoryScope
import com.dust.exweather.ui.fragments.aboutfragments.AboutUsFragment
import com.dust.exweather.ui.fragments.others.ForecastDetailsFragment
import com.dust.exweather.ui.fragments.others.HistoryDetailsFragment
import com.dust.exweather.ui.fragments.others.WeatherDetailsFragment
import com.dust.exweather.ui.fragments.settingfragments.AddLocationFragment
import com.dust.exweather.ui.fragments.settingfragments.GeneralSettingsFragment
import com.dust.exweather.ui.fragments.settingfragments.WeatherSettingsFragment
import com.dust.exweather.ui.fragments.weatherfragments.CurrentWeatherFragment
import com.dust.exweather.ui.fragments.weatherfragments.WeatherForecastFragment
import com.dust.exweather.ui.fragments.weatherfragments.WeatherHistoryFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ContributeMainFragments {
    @ContributesAndroidInjector
    abstract fun contributeCurrentWeatherFragment(): CurrentWeatherFragment

    @ContributesAndroidInjector
    @WeatherHistoryScope
    abstract fun contributeHistoryWeatherFragment(): WeatherHistoryFragment

    @ContributesAndroidInjector
    abstract fun contributePredictionWeatherFragment(): WeatherForecastFragment

    @ContributesAndroidInjector
    abstract fun contributeGeneralSettingsFragment(): GeneralSettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeWeatherSettingsFragment(): WeatherSettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeAboutUsFragment(): AboutUsFragment

    @ContributesAndroidInjector
    abstract fun contributeWeatherDetailsFragment(): WeatherDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeForecastDetailsFragment(): ForecastDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeHistoryDetailsFragment(): HistoryDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeAddLocationFragment(): AddLocationFragment

}