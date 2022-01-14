package com.dust.exweather.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.LiveData
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.ui.fragments.DetailsViewPagerFragment

class DetailsViewPagerAdapter(
    fragmentManager: FragmentManager,
    val dataList: LiveData<List<WeatherEntity>>,
    val dataCount: Int
) : FragmentPagerAdapter(
    fragmentManager
) {
    override fun getCount(): Int = dataCount

    override fun getItem(position: Int): Fragment = DetailsViewPagerFragment(dataList, position)

}