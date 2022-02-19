package com.dust.exweather.ui.fragments.others

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.historyweather.Forecastday
import com.dust.exweather.model.toDataClass
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.sharedpreferences.UnitManager
import com.dust.exweather.ui.adapters.HistoryHourlyRecyclerViewAdapter
import com.dust.exweather.utils.Constants
import com.dust.exweather.utils.UtilityFunctions
import com.dust.exweather.viewmodel.factories.HistoryDetailsViewModelFactory
import com.dust.exweather.viewmodel.fragments.HistoryDetailsFragmentViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_forecast_details.view.*
import javax.inject.Inject

class HistoryDetailsFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: HistoryDetailsViewModelFactory

    @Inject
    lateinit var unitManager: UnitManager

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    private lateinit var viewModel: HistoryDetailsFragmentViewModel

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
        observeCacheData()
        setUpChartsTitles()
    }

    private fun setUpChartsTitles() {
        requireView().apply {
            if (sharedPreferencesManager.getWeatherUnit(Constants.PRECIPITATION_UNIT) != Constants.MM)
                precipitationChartText.text = "نمودار میزان بارندگی 24 ساعته(In)"

            if (sharedPreferencesManager.getWeatherUnit(Constants.TEMPERATURE_UNIT) != Constants.C_PERCENTAGE)
                tempChartText.text = "نمودار دمای 24 ساعته(F°)"

            if (sharedPreferencesManager.getWeatherUnit(Constants.WIND_SPEED_UNIT) != Constants.KPH)
                windSpeedChartText.text = "نمودار سرعت باد 24 ساعته(Mph)"
        }
    }

    private fun observeCacheData() {
        viewModel.getWeatherLiveDataFromCache().observe(viewLifecycleOwner) { cacheData ->
            cacheData?.let { list ->

                val data = viewModel.calculateCurrentData(
                    list.map { it.toDataClass() }, requireArguments().getString(
                        "latlong"
                    ),
                    requireArguments().getString("date")
                )

                data?.let { forecastDay ->
                    setUpUi(forecastDay)
                }
            }
        }
    }

    private fun setUpUi(forecastDay: Forecastday) {
        requireView().apply {
            // disable swipe refresh layout
            forecastDetailsSwipeRefreshLayout.isEnabled = false
            // setup header ui
            forecastDetailsText.text = getString(R.string.historyDetails)
            hourlyForecastTextView.text = getString(R.string.dailyHistory)

            Glide.with(requireContext()).load(forecastDay.day.condition.icon).into(cloudImage)
            dateText.text =
                "${
                    UtilityFunctions.getDayOfWeekByUnixTimeStamp(
                        forecastDay.date_epoch,
                        requireContext()
                    )
                } \n" +
                        "${UtilityFunctions.calculateCurrentDateByTimeEpoch(forecastDay.date_epoch)}"
            weatherStateText.text = forecastDay.day.condition.text
            weatherCityNameText.text = requireArguments().getString("location")
            weatherTempText.text = unitManager.getTemperatureUnit(
                forecastDay.day.avgtemp_c.toString(),
                forecastDay.day.avgtemp_f.toString()
            )
            lastUpdateImage.setImageResource(R.drawable.ic_uv_index)
            lastUpdateText.text = forecastDay.day.uv.toString()
            weatherHumidityText.text = requireContext().resources.getString(
                R.string.humidityText,
                forecastDay.day.avghumidity.toString()
            )
            precipText.text = unitManager.getPrecipitationUnit(
                forecastDay.day.totalprecip_mm.toString(),
                forecastDay.day.totalprecip_in.toString()
            )
            windSpeedText.text = unitManager.getWindSpeedUnit(
                forecastDay.day.maxwind_kph.toString(),
                forecastDay.day.maxwind_mph.toString()
            )
            visibilityTextView.text = unitManager.getVisibilityUnit(
                forecastDay.day.avgvis_km.toString(),
                forecastDay.day.avgvis_miles.toString()
            )
            minTemperatureText.text = unitManager.getTemperatureUnit(
                forecastDay.day.mintemp_c.toString(),
                forecastDay.day.mintemp_f.toString()
            )
            maxTemperatureText.text = unitManager.getTemperatureUnit(
                forecastDay.day.maxtemp_c.toString(),
                forecastDay.day.maxtemp_f.toString()
            )

            // setup details textview
            forecastDetailsTextView.text =
                viewModel.calculateHistoryWeatherDetailsData(requireContext(), forecastDay)


            // setup RecyclerView
            hourlyForecastRecyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            val adapter =
                HistoryHourlyRecyclerViewAdapter(arrayListOf(), requireContext(), unitManager)
            hourlyForecastRecyclerView.adapter = adapter
            adapter.setNewData(forecastDay.hour)

            // setup charts
            setupChartsData(forecastDay)
        }
    }

    private fun setupChartsData(forecastDay: Forecastday) {

        requireView().apply {
            precipitationLineChart.apply {
                setViewPortOffsets(80f, 20f, 40f, 110f)
                setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.full_transparent
                    )
                )
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = false
                setScaleEnabled(false)
                setPinchZoom(false)
                xAxis.apply {
                    labelCount = forecastDay.hour.size
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(true)
                    axisLineColor = Color.WHITE
                    textColor = Color.WHITE
                    legend.isEnabled = true
                    layoutDirection = View.LAYOUT_DIRECTION_RTL
                    animateXY(2000, 2000)
                    valueFormatter = object : IndexAxisValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return if (value.toString().contains(".0") && (value.toInt() % 3) == 0)
                                UtilityFunctions.calculateCurrentTimeByTimeEpoch(forecastDay.hour[value.toInt()].time_epoch)
                            else
                                ""
                        }
                    }
                    invalidate()
                }
                axisRight.isEnabled = false
                axisLeft.apply {
                    labelCount = 6
                    setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                    setDrawGridLines(false)
                    axisLineColor = Color.WHITE
                    textColor = Color.WHITE
                    legend.isEnabled = true
                    animateXY(2000, 2000)
                    invalidate()
                }

                val precipitationDataList = arrayListOf<Entry>()
                if (sharedPreferencesManager.getWeatherUnit(Constants.PRECIPITATION_UNIT) == Constants.MM) {
                    for (i in forecastDay.hour.indices)
                        precipitationDataList.add(
                            Entry(
                                i.toFloat(),
                                forecastDay.hour[i].precip_mm.toFloat()
                            )
                        )
                } else {
                    for (i in forecastDay.hour.indices)
                        precipitationDataList.add(
                            Entry(
                                i.toFloat(),
                                forecastDay.hour[i].precip_in.toFloat()
                            )
                        )
                }

                val lineDataSet =
                    LineDataSet(precipitationDataList, getString(R.string.totalPrecipitation))
                val chartColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.standardUiBlue
                )
                lineDataSet.apply {
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    cubicIntensity = 0.2f
                    setDrawFilled(true)
                    setDrawCircles(true)
                    lineWidth = 1.8f
                    setDrawValues(true)
                    circleRadius = 4f
                    circleColors =
                        arrayListOf(
                            chartColor
                        )
                    highLightColor = chartColor
                    color = chartColor
                    fillColor = chartColor
                    fillAlpha = 100
                    setDrawHorizontalHighlightIndicator(true)
                    fillFormatter =
                        IFillFormatter { _, _ -> axisLeft.axisMinimum }
                }
                val lineData = LineData(lineDataSet)
                lineData.apply {
                    setValueTextSize(9f)
                    setDrawValues(false)
                    setValueTextColor(Color.WHITE)
                }
                data = lineData
            }

            temperatureLineChart.apply {
                setViewPortOffsets(80f, 20f, 40f, 110f)
                setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.full_transparent
                    )
                )
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = false
                setScaleEnabled(false)
                setPinchZoom(false)
                xAxis.apply {
                    labelCount = forecastDay.hour.size
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(true)
                    axisLineColor = Color.WHITE
                    textColor = Color.WHITE
                    legend.isEnabled = true
                    layoutDirection = View.LAYOUT_DIRECTION_RTL
                    animateXY(2000, 2000)
                    valueFormatter = object : IndexAxisValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return if (value.toString().contains(".0") && (value.toInt() % 3) == 0)
                                UtilityFunctions.calculateCurrentTimeByTimeEpoch(forecastDay.hour[value.toInt()].time_epoch)
                            else
                                ""
                        }
                    }
                    invalidate()
                }
                axisRight.isEnabled = false
                axisLeft.apply {
                    labelCount = 6
                    setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                    setDrawGridLines(false)
                    axisLineColor = Color.WHITE
                    textColor = Color.WHITE
                    legend.isEnabled = true
                    animateXY(2000, 2000)
                    invalidate()
                }

                val tempDataList = arrayListOf<Entry>()

                if (sharedPreferencesManager.getWeatherUnit(Constants.TEMPERATURE_UNIT) == Constants.C_PERCENTAGE) {
                    for (i in forecastDay.hour.indices)
                        tempDataList.add(Entry(i.toFloat(), forecastDay.hour[i].temp_c.toFloat()))
                } else {
                    for (i in forecastDay.hour.indices)
                        tempDataList.add(Entry(i.toFloat(), forecastDay.hour[i].temp_f.toFloat()))
                }

                val lineDataSet = LineDataSet(tempDataList, getString(R.string.temperature))
                val chartColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.standardUiYellow
                )
                lineDataSet.apply {
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    cubicIntensity = 0.2f
                    setDrawFilled(true)
                    setDrawCircles(true)
                    lineWidth = 1.8f
                    setDrawValues(true)
                    circleRadius = 4f
                    circleColors =
                        arrayListOf(
                            chartColor
                        )
                    highLightColor = chartColor
                    color = chartColor
                    fillColor = chartColor
                    fillAlpha = 100
                    setDrawHorizontalHighlightIndicator(true)
                    fillFormatter =
                        IFillFormatter { _, _ -> axisLeft.axisMinimum }
                }
                val lineData = LineData(lineDataSet)
                lineData.apply {
                    setValueTextSize(9f)
                    setDrawValues(false)
                    setValueTextColor(Color.WHITE)
                }
                data = lineData
            }

            windSpeedLineChart.apply {
                setViewPortOffsets(80f, 20f, 40f, 110f)
                setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.full_transparent
                    )
                )
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = false
                setScaleEnabled(false)
                setPinchZoom(false)
                xAxis.apply {
                    labelCount = forecastDay.hour.size
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(true)
                    axisLineColor = Color.WHITE
                    textColor = Color.WHITE
                    legend.isEnabled = true
                    layoutDirection = View.LAYOUT_DIRECTION_RTL
                    animateXY(2000, 2000)
                    valueFormatter = object : IndexAxisValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return if (value.toString().contains(".0") && (value.toInt() % 3) == 0)
                                UtilityFunctions.calculateCurrentTimeByTimeEpoch(forecastDay.hour[value.toInt()].time_epoch)
                            else
                                ""
                        }
                    }
                    invalidate()
                }
                axisRight.isEnabled = false
                axisLeft.apply {
                    labelCount = 6
                    setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                    setDrawGridLines(false)
                    axisLineColor = Color.WHITE
                    textColor = Color.WHITE
                    legend.isEnabled = true
                    animateXY(2000, 2000)
                    invalidate()
                }

                val windSpeedDataList = arrayListOf<Entry>()
                if (sharedPreferencesManager.getWeatherUnit(Constants.WIND_SPEED_UNIT) == Constants.KPH) {
                    for (i in forecastDay.hour.indices)
                        windSpeedDataList.add(
                            Entry(
                                i.toFloat(),
                                forecastDay.hour[i].wind_kph.toFloat()
                            )
                        )
                } else {
                    for (i in forecastDay.hour.indices)
                        windSpeedDataList.add(
                            Entry(
                                i.toFloat(),
                                forecastDay.hour[i].wind_mph.toFloat()
                            )
                        )
                }

                val lineDataSet = LineDataSet(windSpeedDataList, getString(R.string.windSpeed))
                val chartColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.windSpeedColor
                )
                lineDataSet.apply {
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    cubicIntensity = 0.2f
                    setDrawFilled(true)
                    setDrawCircles(true)
                    lineWidth = 1.8f
                    setDrawValues(true)
                    circleRadius = 4f
                    circleColors =
                        arrayListOf(
                            chartColor
                        )
                    highLightColor = chartColor
                    color = chartColor
                    fillColor = chartColor
                    fillAlpha = 100
                    setDrawHorizontalHighlightIndicator(true)
                    fillFormatter =
                        IFillFormatter { _, _ -> axisLeft.axisMinimum }
                }
                val lineData = LineData(lineDataSet)
                lineData.apply {
                    setValueTextSize(9f)
                    setDrawValues(false)
                    setValueTextColor(Color.WHITE)
                }
                data = lineData
            }
        }
    }


    private fun setUpViewModel() {
        viewModel =
            ViewModelProvider(this, viewModelFactory)[HistoryDetailsFragmentViewModel::class.java]
    }
}