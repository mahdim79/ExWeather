package com.dust.exweather.ui.fragments.others

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.forecastweather.Forecastday
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.toDataClass
import com.dust.exweather.viewmodel.factories.CurrentFragmentViewModelFactory
import com.dust.exweather.viewmodel.fragments.CurrentFragmentViewModel
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ForecastDetailsFragment : DaggerFragment() {

    private lateinit var viewModel: CurrentFragmentViewModel

    @Inject
    lateinit var viewModelFactory: CurrentFragmentViewModelFactory

    private var firstData = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forecast_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        setUpUi()

    }

    private fun setUpUi() {
        viewModel.getLiveWeatherDataFromCache().observe(viewLifecycleOwner) { data ->
            if (!data.isNullOrEmpty())
                calculateCurrentData(data.map { it.toDataClass() })?.let { forecastDay ->
                    if (firstData)
                        setUpPrimaryUi(forecastDay)
                    else
                        updateUi(forecastDay)
                    firstData = false
                }
        }
    }

    private fun updateUi(forecastDay: Forecastday) {

    }

    private fun setUpPrimaryUi(forecastDay: Forecastday) {

    }

    private fun calculateCurrentData(listData: List<MainWeatherData>): Forecastday? {
        val location = requireArguments().getString("location")
        val date = requireArguments().getString("date")
        if (!location.isNullOrEmpty() && !date.isNullOrEmpty()) {
            listData.forEach { mainWeatherData ->
                if (mainWeatherData.location == location) {
                    mainWeatherData.forecastDetailsData?.let { weatherForecast ->
                        weatherForecast.forecast.forecastday.forEach { forecastDay ->
                            if (forecastDay.date == date)
                                return forecastDay
                        }
                    }
                }
            }
        }
        return null
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[CurrentFragmentViewModel::class.java]
    }
}