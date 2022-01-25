package com.dust.exweather.ui.fragments.viewpagersfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.dust.exweather.R
import com.dust.exweather.model.room.WeatherEntity

class ForecastDetailsViewPagerFragment(
    private val data: LiveData<List<WeatherEntity>>,
    private val progressStateLiveData: LiveData<Boolean>
) :
    Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forecast_details_main_viewpager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpPrimaryUi()
        observeLiveData()
    }

    private fun observeLiveData() {

        data.observe(viewLifecycleOwner) {

        }

        progressStateLiveData.observe(viewLifecycleOwner) {

        }

    }

    private fun setUpPrimaryUi() {
        requireView().apply {

        }
    }
}