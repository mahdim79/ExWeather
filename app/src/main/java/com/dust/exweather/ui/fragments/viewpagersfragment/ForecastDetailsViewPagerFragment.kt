package com.dust.exweather.ui.fragments.viewpagersfragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.ScaleAnimation
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.forecastweather.WeatherForecast
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.sharedpreferences.UnitManager
import com.dust.exweather.ui.adapters.ForecastMainRecyclerViewAdapter
import com.dust.exweather.ui.adapters.TodaysForecastRecyclerViewAdapter
import com.dust.exweather.utils.Constants
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.android.synthetic.main.fragment_forecast_details_main_viewpager.*
import kotlinx.android.synthetic.main.fragment_forecast_details_main_viewpager.view.*
import kotlinx.android.synthetic.main.fragment_forecast_weather.*

class ForecastDetailsViewPagerFragment(
    private val data: LiveData<List<WeatherEntity>>,
    private val position: Int,
    private val unitManager: UnitManager,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val alphaAnimation: AlphaAnimation
) :
    Fragment() {

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
        firstData = true
        observeLiveData()
        setupChartsTitles()
    }

    private fun setupChartsTitles() {
        requireView().apply {
            if (sharedPreferencesManager.getWeatherUnit(Constants.PRECIPITATION_UNIT) != Constants.MM)
                precipChartText.text = getString(R.string.hourlyPrecipitationChartIn)

            if (sharedPreferencesManager.getWeatherUnit(Constants.TEMPERATURE_UNIT) != Constants.C_PERCENTAGE)
                tempChartText.text = getString(R.string.hourlyTemperatureChartF)

            if (sharedPreferencesManager.getWeatherUnit(Constants.WIND_SPEED_UNIT) != Constants.KPH)
                windSpeedChartText.text = getString(R.string.hourlyWindSpeedChartMph)

        }
    }

    private fun observeLiveData() {

        data.observe(viewLifecycleOwner) { data ->
            calculateCurrentPositionData(data)?.let {
                setUpUi(
                    it,
                    data[position].toDataClass().location
                )
            }
        }

    }

    private fun setUpUi(currentData: WeatherForecast, currentLocation: String) {
        if (firstData)
            setUpPrimaryUi(currentData, currentLocation)
        else
            updateCurrentUi(currentData)
        firstData = false
        forecastDetailsContainer.visibility = View.VISIBLE
        showAnimation()

    }

    private fun showAnimation() {
        requireParentFragment().mainContainerView.visibility = View.VISIBLE
        requireParentFragment().mainContainerView.startAnimation(alphaAnimation)
    }

    private fun updateCurrentUi(currentData: WeatherForecast) {
        location_textView.text = currentData.location.name
        updateRecyclerViewData(currentData)
        updateChartData(currentData)
    }

    private fun updateRecyclerViewData(currentData: WeatherForecast) {
        // update recyclerViews data
        if (!currentData.forecast.forecastday.isNullOrEmpty()) {
            mainRecyclerViewAdapter.setNewList(currentData.forecast.forecastday)

            if (!currentData.forecast.forecastday[0].hour.isNullOrEmpty())
                todaysForecastRecyclerViewAdapter.setNewData(currentData.forecast.forecastday[0].hour)
        }
    }

    private fun updateChartData(currentData: WeatherForecast) {
        requireView().apply {
            if (precipitationLineChart.data != null && precipitationLineChart.data.dataSetCount > 0) {
                val dataList = arrayListOf<Entry>()

                if (sharedPreferencesManager.getWeatherUnit(Constants.PRECIPITATION_UNIT) == Constants.MM) {
                    for (i in currentData.forecast.forecastday.indices)
                        dataList.add(
                            Entry(
                                i.toFloat(),
                                currentData.forecast.forecastday[i].day.totalprecip_mm.toFloat()
                            )
                        )
                } else {
                    for (i in currentData.forecast.forecastday.indices)
                        dataList.add(
                            Entry(
                                i.toFloat(),
                                currentData.forecast.forecastday[i].day.totalprecip_in.toFloat()
                            )
                        )
                }

                val dataSet = precipitationLineChart.data.getDataSetByIndex(0) as LineDataSet
                dataSet.values = dataList
                precipitationLineChart.apply {
                    data.notifyDataChanged()
                    notifyDataSetChanged()
                }
            }

            if (temperatureLineChart.data != null && temperatureLineChart.data.dataSetCount > 0) {
                val minTempDataList = arrayListOf<Entry>()
                val avgTempDataList = arrayListOf<Entry>()
                val maxTempDataList = arrayListOf<Entry>()

                if (sharedPreferencesManager.getWeatherUnit(Constants.TEMPERATURE_UNIT) == Constants.C_PERCENTAGE) {
                    for (i in currentData.forecast.forecastday.indices)
                        minTempDataList.add(
                            Entry(
                                i.toFloat(),
                                currentData.forecast.forecastday[i].day.mintemp_c.toFloat()
                            )
                        )

                    for (i in currentData.forecast.forecastday.indices)
                        avgTempDataList.add(
                            Entry(
                                i.toFloat(),
                                currentData.forecast.forecastday[i].day.avgtemp_c.toFloat()
                            )
                        )

                    for (i in currentData.forecast.forecastday.indices)
                        maxTempDataList.add(
                            Entry(
                                i.toFloat(),
                                currentData.forecast.forecastday[i].day.maxtemp_c.toFloat()
                            )
                        )

                } else {
                    for (i in currentData.forecast.forecastday.indices)
                        minTempDataList.add(
                            Entry(
                                i.toFloat(),
                                currentData.forecast.forecastday[i].day.mintemp_f.toFloat()
                            )
                        )

                    for (i in currentData.forecast.forecastday.indices)
                        avgTempDataList.add(
                            Entry(
                                i.toFloat(),
                                currentData.forecast.forecastday[i].day.avgtemp_f.toFloat()
                            )
                        )

                    for (i in currentData.forecast.forecastday.indices)
                        maxTempDataList.add(
                            Entry(
                                i.toFloat(),
                                currentData.forecast.forecastday[i].day.maxtemp_f.toFloat()
                            )
                        )
                }

                (temperatureLineChart.data.getDataSetByIndex(0) as LineDataSet).values =
                    minTempDataList

                (temperatureLineChart.data.getDataSetByIndex(1) as LineDataSet).values =
                    avgTempDataList

                (temperatureLineChart.data.getDataSetByIndex(2) as LineDataSet).values =
                    maxTempDataList

                temperatureLineChart.apply {
                    data.notifyDataChanged()
                    notifyDataSetChanged()
                }

            }

            if (humidityLineChart.data != null && humidityLineChart.data.dataSetCount > 0) {
                val dataList = arrayListOf<Entry>()
                for (i in currentData.forecast.forecastday.indices)
                    dataList.add(
                        Entry(
                            i.toFloat(),
                            currentData.forecast.forecastday[i].day.avghumidity.toFloat()
                        )
                    )
                val dataSet = humidityLineChart.data.getDataSetByIndex(0) as LineDataSet
                dataSet.values = dataList
                humidityLineChart.apply {
                    data.notifyDataChanged()
                    notifyDataSetChanged()
                }
            }

            if (windSpeedLineChart.data != null && windSpeedLineChart.data.dataSetCount > 0) {
                val dataList = arrayListOf<Entry>()

                if (sharedPreferencesManager.getWeatherUnit(Constants.WIND_SPEED_UNIT) == Constants.KPH) {
                    for (i in currentData.forecast.forecastday.indices)
                        dataList.add(
                            Entry(
                                i.toFloat(),
                                currentData.forecast.forecastday[i].day.maxwind_kph.toFloat()
                            )
                        )
                } else {
                    for (i in currentData.forecast.forecastday.indices)
                        dataList.add(
                            Entry(
                                i.toFloat(),
                                currentData.forecast.forecastday[i].day.maxwind_mph.toFloat()
                            )
                        )
                }

                val dataSet = windSpeedLineChart.data.getDataSetByIndex(0) as LineDataSet
                dataSet.values = dataList
                windSpeedLineChart.apply {
                    data.notifyDataChanged()
                    notifyDataSetChanged()
                }
            }
        }
    }

    private fun setUpPrimaryUi(currentData: WeatherForecast, currentLocation: String) {
        setUpPrimaryMainRecyclerView(currentLocation)
        setUpPrimaryTodayHourlyForecastRecyclerView()
        setUpCharts(currentData)
        updateCurrentUi(currentData)
    }

    private fun setUpCharts(currentData: WeatherForecast) {
        setUpPrecipitationChart(currentData)
        setUpTemperatureChart(currentData)
        setUpHumidityChart(currentData)
        setUpWindSpeedChart(currentData)
    }

    private fun setUpPrecipitationChart(currentData: WeatherForecast) {
        requireView().precipitationLineChart.apply {

            setViewPortOffsets(80f, 30f, 40f, 110f)
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
                labelCount = currentData.forecast.forecastday.size
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(true)
                axisLineColor = Color.WHITE
                textColor = Color.WHITE
                legend.isEnabled = true
                legend.textColor = Color.WHITE
                layoutDirection = View.LAYOUT_DIRECTION_RTL
                animateXY(2000, 2000)
                valueFormatter = object : IndexAxisValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value.toString().contains(".0"))
                            currentData.forecast.forecastday[value.toInt()].day.dayOfWeek
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

            val dataList = arrayListOf<Entry>()
            if (sharedPreferencesManager.getWeatherUnit(Constants.PRECIPITATION_UNIT) == Constants.MM) {
                for (i in currentData.forecast.forecastday.indices)
                    dataList.add(
                        Entry(
                            i.toFloat(),
                            currentData.forecast.forecastday[i].day.totalprecip_mm.toFloat()
                        )
                    )
            } else {
                for (i in currentData.forecast.forecastday.indices)
                    dataList.add(
                        Entry(
                            i.toFloat(),
                            currentData.forecast.forecastday[i].day.totalprecip_in.toFloat()
                        )
                    )
            }


            val lineDataSet = LineDataSet(dataList, getString(R.string.dailyPrecipitation))
            lineDataSet.apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.2f
                setDrawFilled(true)
                setDrawCircles(true)
                lineWidth = 1.8f
                setDrawValues(true)
                circleRadius = 4f
                circleColors = arrayListOf(Color.BLUE)
                highLightColor = Color.BLUE
                color = Color.BLUE
                fillColor = Color.BLUE
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

    private fun setUpTemperatureChart(currentData: WeatherForecast) {
        requireView().temperatureLineChart.apply {

            setViewPortOffsets(80f, 10f, 40f, 110f)
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
                labelCount = currentData.forecast.forecastday.size
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(true)
                axisLineColor = Color.WHITE
                textColor = Color.WHITE
                legend.isEnabled = true
                legend.textColor = Color.WHITE
                layoutDirection = View.LAYOUT_DIRECTION_RTL
                animateXY(2000, 2000)
                valueFormatter = object : IndexAxisValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value.toString().contains(".0"))
                            currentData.forecast.forecastday[value.toInt()].day.dayOfWeek
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

            val minTempDataList = arrayListOf<Entry>()
            val avgTempDataList = arrayListOf<Entry>()
            val maxTempDataList = arrayListOf<Entry>()

            if (sharedPreferencesManager.getWeatherUnit(Constants.TEMPERATURE_UNIT) == Constants.C_PERCENTAGE) {
                for (i in currentData.forecast.forecastday.indices)
                    minTempDataList.add(
                        Entry(
                            i.toFloat(),
                            currentData.forecast.forecastday[i].day.mintemp_c.toFloat()
                        )
                    )

                for (i in currentData.forecast.forecastday.indices)
                    avgTempDataList.add(
                        Entry(
                            i.toFloat(),
                            currentData.forecast.forecastday[i].day.avgtemp_c.toFloat()
                        )
                    )

                for (i in currentData.forecast.forecastday.indices)
                    maxTempDataList.add(
                        Entry(
                            i.toFloat(),
                            currentData.forecast.forecastday[i].day.maxtemp_c.toFloat()
                        )
                    )
            } else {
                for (i in currentData.forecast.forecastday.indices)
                    minTempDataList.add(
                        Entry(
                            i.toFloat(),
                            currentData.forecast.forecastday[i].day.mintemp_f.toFloat()
                        )
                    )

                for (i in currentData.forecast.forecastday.indices)
                    avgTempDataList.add(
                        Entry(
                            i.toFloat(),
                            currentData.forecast.forecastday[i].day.avgtemp_f.toFloat()
                        )
                    )

                for (i in currentData.forecast.forecastday.indices)
                    maxTempDataList.add(
                        Entry(
                            i.toFloat(),
                            currentData.forecast.forecastday[i].day.maxtemp_f.toFloat()
                        )
                    )
            }

            val minTempDataSet = LineDataSet(minTempDataList, getString(R.string.minimum))
            val chartColor = ContextCompat.getColor(requireContext(), R.color.standardUiRed)
            minTempDataSet.apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.2f
                setDrawFilled(true)
                setDrawCircles(true)
                lineWidth = 1.8f
                setDrawValues(true)
                circleRadius = 4f
                circleColors =
                    arrayListOf(chartColor)
                highLightColor = chartColor
                color = chartColor
                fillColor = chartColor
                fillAlpha = 100
                setDrawHorizontalHighlightIndicator(true)
                fillFormatter =
                    IFillFormatter { _, _ -> axisLeft.axisMinimum }
            }

            val avgTempDataSet = LineDataSet(avgTempDataList, getString(R.string.average))
            avgTempDataSet.apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.2f
                setDrawFilled(true)
                setDrawCircles(true)
                lineWidth = 1.8f
                setDrawValues(true)
                circleRadius = 4f
                circleColors =
                    arrayListOf(ContextCompat.getColor(requireContext(), R.color.standardUiYellow))
                highLightColor = ContextCompat.getColor(requireContext(), R.color.standardUiYellow)
                color = ContextCompat.getColor(requireContext(), R.color.standardUiYellow)
                fillColor = ContextCompat.getColor(requireContext(), R.color.standardUiYellow)
                fillAlpha = 100
                setDrawHorizontalHighlightIndicator(true)
                fillFormatter =
                    IFillFormatter { _, _ -> axisLeft.axisMinimum }
            }

            val maxTempDataSet = LineDataSet(maxTempDataList, getString(R.string.maximum))
            maxTempDataSet.apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.2f
                setDrawFilled(true)
                setDrawCircles(true)
                lineWidth = 1.8f
                setDrawValues(true)
                circleRadius = 4f
                circleColors =
                    arrayListOf(ContextCompat.getColor(requireContext(), R.color.standardUiBlue))
                highLightColor = ContextCompat.getColor(requireContext(), R.color.standardUiBlue)
                color = ContextCompat.getColor(requireContext(), R.color.standardUiBlue)
                fillColor = ContextCompat.getColor(requireContext(), R.color.standardUiBlue)
                fillAlpha = 100
                setDrawHorizontalHighlightIndicator(true)
                fillFormatter =
                    IFillFormatter { _, _ -> axisLeft.axisMinimum }
            }

            val lineData = LineData(minTempDataSet, avgTempDataSet, maxTempDataSet)
            lineData.apply {
                setValueTextSize(9f)
                setDrawValues(false)
                setValueTextColor(Color.WHITE)
            }
            data = lineData
        }
    }

    private fun setUpHumidityChart(currentData: WeatherForecast) {
        requireView().humidityLineChart.apply {

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
                labelCount = currentData.forecast.forecastday.size
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(true)
                axisLineColor = Color.WHITE
                textColor = Color.WHITE
                legend.isEnabled = true
                legend.textColor = Color.WHITE
                layoutDirection = View.LAYOUT_DIRECTION_RTL
                animateXY(2000, 2000)
                valueFormatter = object : IndexAxisValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value.toString().contains(".0"))
                            currentData.forecast.forecastday[value.toInt()].day.dayOfWeek
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

            val dataList = arrayListOf<Entry>()
            for (i in currentData.forecast.forecastday.indices)
                dataList.add(
                    Entry(
                        i.toFloat(),
                        currentData.forecast.forecastday[i].day.avghumidity.toFloat()
                    )
                )
            val lineDataSet = LineDataSet(dataList, getString(R.string.airHumidity))
            val chartColor = ContextCompat.getColor(requireContext(), R.color.humidityColor)
            lineDataSet.apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.2f
                setDrawFilled(true)
                setDrawCircles(true)
                lineWidth = 1.8f
                setDrawValues(true)
                circleRadius = 4f
                circleColors =
                    arrayListOf(chartColor)
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

    private fun setUpWindSpeedChart(currentData: WeatherForecast) {
        requireView().windSpeedLineChart.apply {

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
                labelCount = currentData.forecast.forecastday.size
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(true)
                axisLineColor = Color.WHITE
                textColor = Color.WHITE
                legend.isEnabled = true
                legend.textColor = Color.WHITE
                layoutDirection = View.LAYOUT_DIRECTION_RTL
                animateXY(2000, 2000)
                valueFormatter = object : IndexAxisValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value.toString().contains(".0"))
                            currentData.forecast.forecastday[value.toInt()].day.dayOfWeek
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

            val dataList = arrayListOf<Entry>()

            if (sharedPreferencesManager.getWeatherUnit(Constants.WIND_SPEED_UNIT) == Constants.KPH) {
                for (i in currentData.forecast.forecastday.indices)
                    dataList.add(
                        Entry(
                            i.toFloat(),
                            currentData.forecast.forecastday[i].day.maxwind_kph.toFloat()
                        )
                    )
            } else {
                for (i in currentData.forecast.forecastday.indices)
                    dataList.add(
                        Entry(
                            i.toFloat(),
                            currentData.forecast.forecastday[i].day.maxwind_mph.toFloat()
                        )
                    )
            }

            val lineDataSet = LineDataSet(dataList, getString(R.string.windSpeed))
            val chartColor = ContextCompat.getColor(requireContext(), R.color.windSpeedColor)
            lineDataSet.apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.2f
                setDrawFilled(true)
                setDrawCircles(true)
                lineWidth = 1.8f
                setDrawValues(true)
                circleRadius = 4f
                circleColors =
                    arrayListOf(chartColor)
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

    private fun setUpPrimaryTodayHourlyForecastRecyclerView() {
        todaysHourlyForecastRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        todaysForecastRecyclerViewAdapter =
            TodaysForecastRecyclerViewAdapter(
                arrayListOf(),
                requireContext(),
                unitManager
            )

        todaysHourlyForecastRecyclerView.adapter = todaysForecastRecyclerViewAdapter
    }

    private fun setUpPrimaryMainRecyclerView(currentLocation: String) {
        mainRecyclerViewAdapter = ForecastMainRecyclerViewAdapter(
            arrayListOf(),
            requireContext(),
            requireActivity().findNavController(R.id.mainFragmentContainerView),
            currentLocation,
            unitManager
        )
        mainForecastRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        mainForecastRecyclerView.adapter = mainRecyclerViewAdapter
    }

    private fun calculateCurrentPositionData(data: List<WeatherEntity>): WeatherForecast? =
        data[position].toDataClass().forecastDetailsData

}