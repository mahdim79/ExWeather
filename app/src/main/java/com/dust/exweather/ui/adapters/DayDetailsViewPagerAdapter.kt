package com.dust.exweather.ui.adapters

import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.LiveData
import com.dust.exweather.interfaces.DayDetailsViewPagerOnClickListener
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.ui.fragments.viewpagersfragment.currentdetailsfragments.CurrentDetailsViewPagerFragment
import com.dust.exweather.ui.fragments.viewpagersfragment.currentdetailsfragments.ForecastDetailsViewPagerFragment
import com.dust.exweather.ui.fragments.viewpagersfragment.currentdetailsfragments.HistoryDetailsViewPagerFragment

class DayDetailsViewPagerAdapter(
    fragmentManager: FragmentManager,
    private val data: LiveData<List<WeatherEntity>>,
    private val alphaAnimation: AlphaAnimation,
    private val location: String,
    private val onClickListener: DayDetailsViewPagerOnClickListener
) :
    FragmentPagerAdapter(fragmentManager) {
    override fun getCount(): Int = 3

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> HistoryDetailsViewPagerFragment(data, alphaAnimation, location, onClickListener)
            1 -> CurrentDetailsViewPagerFragment(data, alphaAnimation, location, onClickListener)
            else -> ForecastDetailsViewPagerFragment(data, alphaAnimation, location, onClickListener)
        }
    }
}