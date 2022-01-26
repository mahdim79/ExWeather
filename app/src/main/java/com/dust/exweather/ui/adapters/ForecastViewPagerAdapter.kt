package com.dust.exweather.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.LiveData
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.ui.fragments.viewpagersfragment.ForecastDetailsViewPagerFragment

class ForecastViewPagerAdapter(
    fragmentManager: FragmentManager,
    private val data: LiveData<List<WeatherEntity>>,
    private val progressStateLiveData: LiveData<Boolean>,
    private val itemCount: Int
) : FragmentPagerAdapter(fragmentManager) {
    override fun getCount(): Int = itemCount

    override fun getItem(position: Int): Fragment = ForecastDetailsViewPagerFragment(data, progressStateLiveData, position)
}