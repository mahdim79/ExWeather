package com.dust.exweather.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.ui.fragments.viewpagersfragment.MainDetailsViewPagerFragment

class DetailsViewPagerAdapter(
    fragmentManager: FragmentManager,
    val dataList: LiveData<List<WeatherEntity>>,
    val dataCount: Int,
    val progressLiveData:LiveData<Boolean>,
    val navController: NavController
) : FragmentStatePagerAdapter(
    fragmentManager
) {
    override fun getCount(): Int = dataCount

    override fun getItem(position: Int): Fragment = MainDetailsViewPagerFragment(dataList, position, progressLiveData,navController)

}