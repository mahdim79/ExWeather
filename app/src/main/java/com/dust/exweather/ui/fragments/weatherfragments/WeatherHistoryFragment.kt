package com.dust.exweather.ui.fragments.weatherfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.historyweather.Forecastday
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import com.dust.exweather.utils.UtilityFunctions
import com.dust.exweather.viewmodel.factories.HistoryFragmentViewModelFactory
import com.dust.exweather.viewmodel.fragments.HistoryFragmentViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_history_weather.*
import kotlinx.android.synthetic.main.fragment_history_weather.view.*
import java.util.*
import javax.inject.Inject

class WeatherHistoryFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: HistoryFragmentViewModelFactory

    private lateinit var viewModel: HistoryFragmentViewModel

    private var locationIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        observeCacheData()
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[HistoryFragmentViewModel::class.java]
    }

    private fun observeCacheData() {
        viewModel.getLiveWeatherDataFromCache().observe(viewLifecycleOwner) { rawData ->
            rawData?.let {
                viewModel.addHistoryDataToDataList(rawData)
                setUpPrimaryUi()
            }
        }
    }

    private fun setUpPrimaryUi() {
        viewModel.getHistoryDataList().also {
            if (!it.isNullOrEmpty()) {
                updateLocationData()
                setUpLocationSwitcherButtons(it.lastIndex)
                setUpPrimaryCalendarView()
            }
        }
    }

    private fun setUpPrimaryCalendarView() {
        updateCalendarViewTime()
        val calendar = Calendar.getInstance()
        requireView().historyCalendarView.setOnDateChangeListener { _, _, _, dayOfMonth ->
            viewModel.getHistoryDataList()[locationIndex].also { weatherHistory ->
                weatherHistory.forecast.forecastday.forEach { forecastDay ->
                    calendar.time = Date((forecastDay.date_epoch.toLong()) * 1000)
                    if (dayOfMonth == calendar.get(Calendar.DAY_OF_MONTH)) {
                        updateDayDetailsData(forecastDay, weatherHistory.location.name)
                    }
                }
            }
        }

    }

    private fun updateCalendarViewTime() {
        viewModel.getHistoryDataList()[locationIndex].also { weatherHistory ->
            requireView().historyCalendarView.apply {
              //  minDate = weatherHistory.forecast.forecastday.last().date_epoch.toLong() * 1000
                // maxDate = weatherHistory.forecast.forecastday.first().date_epoch.toLong() * 1000
              date = weatherHistory.forecast.forecastday.first().date_epoch.toLong() * 1000
            }
        }
        clearHistoryDetailsData()
    }

    private fun clearHistoryDetailsData() {
        requireView().historyDetailsMainContainer.visibility = View.GONE
    }

    private fun updateDayDetailsData(forecastDay: Forecastday, locationName: String) {
        requireView().apply {
            historyDetailsMainContainer.visibility = View.VISIBLE
            dateTextView.text = "${forecastDay.day.dayOfWeek}\n" +
                    "${UtilityFunctions.calculateCurrentDateByTimeEpoch(forecastDay.date_epoch)}"
            weatherStateText.text = forecastDay.day.condition.text
            Glide.with(requireContext()).load(forecastDay.day.condition.icon).into(cloudImage)
            weatherCityNameText.text = locationName
            visibilityTextView.text = requireContext().resources.getString(
                R.string.visibilityText,
                forecastDay.day.avgvis_km.toString()
            )
            uvIndexTextView.text = forecastDay.day.uv.toString()
            weatherHumidityText.text = requireContext().resources.getString(
                R.string.humidityText,
                forecastDay.day.avghumidity.toString()
            )
            precipText.text = requireContext().resources.getString(
                R.string.precipitationText,
                forecastDay.day.totalprecip_mm.toString()
            )
            windSpeedText.text = requireContext().resources.getString(
                R.string.windSpeedText,
                forecastDay.day.maxwind_kph.toString()
            )
            minTemperatureText.text = requireContext().resources.getString(
                R.string.temperatureText,
                forecastDay.day.mintemp_c.toString()
            )
            avgWeatherTempText.text = requireContext().resources.getString(
                R.string.temperatureText,
                forecastDay.day.avgtemp_c.toString()
            )
            maxTemperatureText.text = requireContext().resources.getString(
                R.string.temperatureText,
                forecastDay.day.maxtemp_c.toString()
            )
        }
    }

    private fun setUpLocationSwitcherButtons(lastIndex: Int) {
        arrowRightIcon.setOnClickListener {
            if (locationIndex != 0) {
                locationIndex--
                updateCalendarViewTime()
                updateLocationData()
            }
        }

        arrowLeftIcon.setOnClickListener {
            if (locationIndex != lastIndex) {
                locationIndex++
                updateCalendarViewTime()
                updateLocationData()
            }
        }
    }

    private fun updateLocationData() {
        viewModel.getHistoryDataList()[locationIndex].apply {
            locationTextView.text = location.name

        }
    }


}