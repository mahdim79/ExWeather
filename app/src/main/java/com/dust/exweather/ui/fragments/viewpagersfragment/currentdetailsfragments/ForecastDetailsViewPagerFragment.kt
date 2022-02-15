package com.dust.exweather.ui.fragments.viewpagersfragment.currentdetailsfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.dust.exweather.R
import com.dust.exweather.interfaces.DayDetailsViewPagerOnClickListener
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import com.dust.exweather.sharedpreferences.UnitManager
import com.dust.exweather.ui.adapters.MainRecyclerViewAdapter
import com.dust.exweather.utils.DataStatus
import kotlinx.android.synthetic.main.fragment_forecast_details_viewpager.view.*

class ForecastDetailsViewPagerFragment(
    private val data: LiveData<List<WeatherEntity>>,
    private val alphaAnimation: AlphaAnimation,
    private val location: String,
    private val onClickListener: DayDetailsViewPagerOnClickListener,
    private val unitManager: UnitManager
) : Fragment() {

    private lateinit var recyclerViewAdapter: MainRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forecast_details_viewpager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUi()
    }

    private fun setUpUi() {
        setUpPrimaryRecyclerView()
        setUpOtherStuff()
        observeData()
    }

    private fun setUpOtherStuff() {
        requireView().apply {
            scrollHelperCardView.setOnClickListener {
                onClickListener.goToCurrentWeatherPage()
            }
        }
    }

    private fun observeData() {
        data.observe(viewLifecycleOwner) {
            val listData = arrayListOf<DataWrapper<Any>>()
            it.forEach { weatherEntity ->
                val data = weatherEntity.toDataClass()
                if (data.location == location)
                    data.forecastDetailsData!!.forecast.forecastday.forEach { forecastDay ->
                        listData.add(DataWrapper(forecastDay, DataStatus.DATA_RECEIVE_SUCCESS))
                    }
            }
            recyclerViewAdapter.setNewData(listData)
        }
    }

    private fun setUpPrimaryRecyclerView() {
        requireView().apply {
            recyclerViewAdapter =
                MainRecyclerViewAdapter(requireContext(), arrayListOf(), alphaAnimation, unitManager)
            forecastDetailsRecyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            forecastDetailsRecyclerView.adapter = recyclerViewAdapter
        }
    }
}