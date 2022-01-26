package com.dust.exweather.ui.fragments.viewpagersfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.forecastweather.WeatherForecast
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import com.dust.exweather.ui.adapters.ForecastMainRecyclerViewAdapter
import com.dust.exweather.ui.adapters.TodaysForecastRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_forecast_details_main_viewpager.*
import kotlinx.android.synthetic.main.fragment_forecast_details_main_viewpager.view.*

class ForecastDetailsViewPagerFragment(
    private val data: LiveData<List<WeatherEntity>>,
    private val progressStateLiveData: LiveData<Boolean>,
    private val position: Int
) :
    Fragment() {

    private var hourlyForecastRecyclerViewScrolled = false

    private lateinit var mainRecyclerViewAdapter: ForecastMainRecyclerViewAdapter

    private lateinit var todaysForecastRecyclerViewAdapter: TodaysForecastRecyclerViewAdapter

    private var firstData = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forecast_details_main_viewpager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeLiveData()
    }

    private fun observeLiveData() {

        data.observe(viewLifecycleOwner) { data ->
            calculateCurrentPositionData(data)?.let { setUpUi(it) }
        }

        progressStateLiveData.observe(viewLifecycleOwner) {
            setLoadingState(it)
        }

    }

    private fun setLoadingState(state: Boolean?) {
        state?.let {
            requireView().itemProgressBar.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun setUpUi(currentData: WeatherForecast) {
        if (firstData)
            setUpPrimaryUi(currentData)
        else
            updateCurrentUi(currentData)
        firstData = false

    }

    private fun updateCurrentUi(currentData: WeatherForecast) {
        location_textView.text = currentData.location.name

        // update recyclerViews data
        if (!currentData.forecast.forecastday.isNullOrEmpty()) {
            mainRecyclerViewAdapter.setNewList(currentData.forecast.forecastday)

            if (!currentData.forecast.forecastday[0].hour.isNullOrEmpty())
                todaysForecastRecyclerViewAdapter.setNewData(currentData.forecast.forecastday[0].hour)
        }
    }

    private fun setUpPrimaryUi(currentData: WeatherForecast) {
        requireView().apply {
            setUpPrimaryMainRecyclerView()
            setUpPrimaryTodayHourlyForecastRecyclerView()
            setUpPrimaryUiStuff()
            updateCurrentUi(currentData)
        }
    }

    private fun setUpPrimaryUiStuff() {
        requireView().apply {

        }
    }

    private fun setUpPrimaryTodayHourlyForecastRecyclerView() {
        todaysHourlyForecastRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        todaysForecastRecyclerViewAdapter =
            TodaysForecastRecyclerViewAdapter(
                arrayListOf(),
                requireContext()
            )

        todaysHourlyForecastRecyclerView.adapter = todaysForecastRecyclerViewAdapter
    }

    private fun setUpPrimaryMainRecyclerView() {
        mainRecyclerViewAdapter = ForecastMainRecyclerViewAdapter(arrayListOf(), requireContext())
        mainForecastRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        mainForecastRecyclerView.adapter = mainRecyclerViewAdapter
    }

    private fun calculateCurrentPositionData(data: List<WeatherEntity>): WeatherForecast? =
        data[position].toDataClass().forecastDetailsData

}