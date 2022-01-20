package com.dust.exweather.ui.fragments.viewpagersfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.dust.exweather.R
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.utils.Constants
import kotlinx.android.synthetic.main.fragment_day_details_viewpager.view.*

class DayDetailsViewPagerFragment(
    private val viewType: Int,
    private val data: LiveData<List<WeatherEntity>>
) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_day_details_viewpager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUi()
    }

    private fun setUpUi() {
        requireView().apply {
            when (viewType) {
                Constants.CURRENT_TYPE -> {
                    textView.text = "current"
                }
                Constants.FORECAST_TYPE -> {
                    textView.text = "forecast"
                }
                Constants.HISTORY_TYPE -> {
                    textView.text = "history"
                }
            }
        }
    }
}