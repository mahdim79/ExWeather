package com.dust.exweather.ui.fragments.weatherfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.historyweather.Forecastday
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

    private val calendar = Calendar.getInstance()

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
                setUpFirstData()
            }
        }
    }

    private fun setUpFirstData() {
        changeDate(UtilityFunctions.calculateDayOfMonth(viewModel.getHistoryDataList()[locationIndex].forecast.forecastday.first().date_epoch))
    }

    private fun setUpPrimaryCalendarView() {
        updateCalendarViewTime()
        requireView().historyCalendarView.setOnDateChangeListener { _, _, _, dayOfMonth ->
            changeDate(dayOfMonth)
        }

    }

    private fun changeDate(dayOfMonth: Int) {
        updateNoDataAvailable()
        viewModel.getHistoryDataList()[locationIndex].also { weatherHistory ->
            weatherHistory.forecast.forecastday.forEach { forecastDay ->
                calendar.time = Date((forecastDay.date_epoch.toLong()) * 1000)
                if (dayOfMonth == calendar.get(Calendar.DAY_OF_MONTH)) {
                    updateDayDetailsData(
                        forecastDay,
                        weatherHistory.location.name,
                        UtilityFunctions.createLatLongPattern(weatherHistory.location)
                    )
                }
            }
        }
    }

    private fun updateCalendarViewTime() {
        viewModel.getHistoryDataList()[locationIndex].also { weatherHistory ->
            requireView().historyCalendarView.apply {
                date = weatherHistory.forecast.forecastday.first().date_epoch.toLong() * 1000
            }
        }
        updateNoDataAvailable()
    }

    private fun updateNoDataAvailable(){
        requireView().apply {
            historyDetailsMainContainer.visibility = View.GONE
            noDataTextView.visibility = View.VISIBLE
        }
    }

    private fun updateDayDetailsData(
        forecastDay: Forecastday,
        locationName: String,
        latlong: String
    ) {
        requireView().apply {
            historyDetailsMainContainer.visibility = View.VISIBLE
            noDataTextView.visibility = View.GONE
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

            hourlyDetailsTextView.setOnClickListener {
                navigateToDetailsFragment(latlong, forecastDay.date, locationName)
            }

            hourlyDetailsImageView.setOnClickListener {
                navigateToDetailsFragment(latlong, forecastDay.date, locationName)
            }

        }
    }

    private fun navigateToDetailsFragment(latlong: String, date: String, location: String) {
        val bundleArgs = Bundle()
        bundleArgs.apply {
            putString("latlong", latlong)
            putString("date", date)
            putString("location", location)
        }
        requireActivity().findNavController(R.id.mainFragmentContainerView).navigate(
            R.id.action_weatherHistoryFragment_to_historyDetailsFragment,
            bundleArgs
        )
    }

    private fun setUpLocationSwitcherButtons(lastIndex: Int) {
        arrowRightIcon.setOnClickListener {
            if (locationIndex != 0) {
                locationIndex--
                updateCalendarViewTime()
                updateLocationData()
                setUpFirstData()
            }
        }

        arrowLeftIcon.setOnClickListener {
            if (locationIndex != lastIndex) {
                locationIndex++
                updateCalendarViewTime()
                updateLocationData()
                setUpFirstData()
            }
        }
    }

    private fun updateLocationData() {
        viewModel.getHistoryDataList()[locationIndex].apply {
            locationTextView.text = location.name

        }
    }


}