package com.dust.exweather.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.LiveData
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.ui.fragments.viewpagersfragment.DayDetailsViewPagerFragment
import com.dust.exweather.utils.Constants

class DayDetailsViewPagerAdapter(
    fragmentManager: FragmentManager,
    private val data: LiveData<List<WeatherEntity>>
) :
    FragmentPagerAdapter(fragmentManager) {
    override fun getCount(): Int = 3

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> DayDetailsViewPagerFragment(Constants.HISTORY_TYPE, data)
            1 -> DayDetailsViewPagerFragment(Constants.CURRENT_TYPE, data)
            else -> DayDetailsViewPagerFragment(Constants.FORECAST_TYPE, data)
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "تاریخچه"
            1 -> "فعلی"
            else -> "پیش بینی"
        }
    }
}