package com.dust.exweather.ui.fragments.others

import android.annotation.SuppressLint
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
import com.dust.exweather.model.dataclasses.forecastweather.Forecastday
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.toDataClass
import com.dust.exweather.ui.adapters.TodaysForecastRecyclerViewAdapter
import com.dust.exweather.utils.DataStatus
import com.dust.exweather.utils.UtilityFunctions
import com.dust.exweather.utils.convertAmPm
import com.dust.exweather.viewmodel.factories.CurrentFragmentViewModelFactory
import com.dust.exweather.viewmodel.fragments.CurrentFragmentViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_current_weather.view.*
import kotlinx.android.synthetic.main.fragment_forecast_details.view.*
import kotlinx.android.synthetic.main.fragment_forecast_details_main_viewpager.view.precipitationLineChart
import kotlinx.android.synthetic.main.fragment_forecast_details_main_viewpager.view.temperatureLineChart
import kotlinx.android.synthetic.main.fragment_forecast_details_main_viewpager.view.windSpeedLineChart
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ForecastDetailsFragment : DaggerFragment() {

    private lateinit var viewModel: CurrentFragmentViewModel

    @Inject
    lateinit var viewModelFactory: CurrentFragmentViewModelFactory

    private lateinit var hourlyForecastRecyclerViewAdapter: TodaysForecastRecyclerViewAdapter

    private var firstData = true

    private val compositeDisposable = CompositeDisposable()

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
        observeForApiCallState()
        observeForCacheData()
    }

    private fun observeForCacheData() {
        viewModel.getLiveWeatherDataFromCache().observe(viewLifecycleOwner) { data ->
            if (!data.isNullOrEmpty())
                calculateCurrentData(data.map { it.toDataClass() })?.let { mainWeatherData ->
                    calculateData(mainWeatherData)?.let { forecastDay ->
                        if (firstData)
                            setUpPrimaryUi(forecastDay)

                        updateUi(
                            forecastDay,
                            mainWeatherData.forecastDetailsData?.location?.name ?: "null",
                            UtilityFunctions.calculateLastUpdateText(
                                mainWeatherData.current?.current?.system_last_update_epoch ?: 0
                            )
                        )

                        firstData = false
                    }
                }
        }
    }

    private fun observeForApiCallState() {
        viewModel.getWeatherApiCallStateLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.DATA_RECEIVE_LOADING -> {
                    setProgressMode(true)
                }

                DataStatus.DATA_RECEIVE_FAILURE -> {
                    setProgressMode(false)
                    resetSwipeRefreshLayout()
                }

                DataStatus.DATA_RECEIVE_SUCCESS -> {
                    setProgressMode(false)
                    resetSwipeRefreshLayout()
                }
            }
        }
    }

    private fun setProgressMode(b: Boolean) {
        val progressBarVisibility = if (b) View.VISIBLE else View.INVISIBLE
        val viewVisibility = if (b) View.INVISIBLE else View.VISIBLE
        requireView().apply {
            progressBarNum1.visibility = progressBarVisibility
            progressBarNum6.visibility = progressBarVisibility
            progressBarNum8.visibility = progressBarVisibility
            progressBarNum9.visibility = progressBarVisibility
            progressBarNum4.visibility = progressBarVisibility
            progressBarNum10.visibility = progressBarVisibility
            progressBarNum7.visibility = progressBarVisibility
            progressBarNum3.visibility = progressBarVisibility

            weatherStateText.visibility = viewVisibility
            weatherCityNameText.visibility = viewVisibility
            lastUpdateText.visibility = viewVisibility
            visibilityTextView.visibility = viewVisibility
            weatherHumidityText.visibility = viewVisibility
            precipText.visibility = viewVisibility
            windSpeedText.visibility = viewVisibility
            minTemperatureText.visibility = viewVisibility
            maxTemperatureText.visibility = viewVisibility
            weatherTempText.visibility = viewVisibility
        }
    }

    private fun resetSwipeRefreshLayout() {
        requireView().forecastDetailsSwipeRefreshLayout.isRefreshing = false
    }

    private fun calculateData(data: MainWeatherData): Forecastday? {
        val date = requireArguments().getString("date")
        data.forecastDetailsData?.let { weatherForecast ->
            weatherForecast.forecast.forecastday.forEach { forecastDay ->
                if (forecastDay.date == date)
                    return forecastDay
            }
        }
        return null
    }

    private fun updateUi(forecastDay: Forecastday, locationName: String, lastUpdateTime: String) {

        updateHeaderAndOtherDetails(forecastDay, locationName, lastUpdateTime)

        updateChartsData(forecastDay)

    }

    private fun updateHeaderAndOtherDetails(
        forecastDay: Forecastday,
        locationName: String,
        lastUpdateTime: String
    ) {
        requireView().apply {
            // update header ui
            Glide.with(requireContext()).load(forecastDay.day.condition.icon).into(cloudImage)
            dateText.text =
                "${UtilityFunctions.getDayOfWeekByUnixTimeStamp(forecastDay.date_epoch)} \n" +
                        "${UtilityFunctions.calculateCurrentDateByTimeEpoch(forecastDay.date_epoch)}"
            weatherStateText.text = forecastDay.day.condition.text
            weatherCityNameText.text = locationName
            weatherTempText.text = requireContext().resources.getString(
                R.string.temperatureText,
                forecastDay.day.avgtemp_c.toString()
            )
            lastUpdateText.text = lastUpdateTime
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
            visibilityTextView.text = requireContext().resources.getString(
                R.string.visibilityText,
                forecastDay.day.avgvis_km.toString()
            )
            minTemperatureText.text = requireContext().resources.getString(
                R.string.temperatureText,
                forecastDay.day.mintemp_c.toString()
            )
            maxTemperatureText.text = requireContext().resources.getString(
                R.string.temperatureText,
                forecastDay.day.maxtemp_c.toString()
            )

            if (forecastDay.day.daily_chance_of_rain >= 50)
                chanceOfRainImageView.visibility = View.VISIBLE

            if (forecastDay.day.daily_chance_of_snow >= 50)
                chanceOfSnowImageView.visibility = View.VISIBLE

            // update recyclerview
            hourlyForecastRecyclerViewAdapter.setNewData(forecastDay.hour)

            // update details textview
            forecastDetailsTextView.text =
                "طلوع آفتاب: ${forecastDay.astro.sunrise.convertAmPm()} \n" +
                        "غروب آفتاب: ${forecastDay.astro.sunset.convertAmPm()} \n" +
                        "طلوع ماه: ${forecastDay.astro.moonrise.convertAmPm()} \n" +
                        "غروب ماه: ${forecastDay.astro.moonset.convertAmPm()} \n" +
                        "روشنایی ماه: ${forecastDay.astro.moon_illumination} \n" +
                        "فاز ماه: ${forecastDay.astro.moon_phase} \n" +
                        " میزان اشعه ماورابنفش: ${forecastDay.day.uv}"
        }
    }

    private fun updateChartsData(forecastDay: Forecastday) {
        requireView().apply {
            precipitationLineChart.apply {
                if (data != null && data.dataSetCount > 0) {
                    val dataList = arrayListOf<Entry>()
                    for (i in forecastDay.hour.indices)
                        dataList.add(Entry(i.toFloat(), forecastDay.hour[i].precip_mm.toFloat()))

                    val chartData = data.getDataSetByIndex(0) as LineDataSet
                    chartData.values = dataList
                    data.notifyDataChanged()
                    notifyDataSetChanged()
                }
            }

            temperatureLineChart.apply {
                if (data != null && data.dataSetCount > 0) {
                    val dataList = arrayListOf<Entry>()
                    for (i in forecastDay.hour.indices)
                        dataList.add(Entry(i.toFloat(), forecastDay.hour[i].temp_c.toFloat()))

                    val chartData = data.getDataSetByIndex(0) as LineDataSet
                    chartData.values = dataList
                    data.notifyDataChanged()
                    notifyDataSetChanged()
                }
            }

            windSpeedLineChart.apply {
                if (data != null && data.dataSetCount > 0) {
                    val dataList = arrayListOf<Entry>()
                    for (i in forecastDay.hour.indices)
                        dataList.add(Entry(i.toFloat(), forecastDay.hour[i].wind_kph.toFloat()))

                    val chartData = data.getDataSetByIndex(0) as LineDataSet
                    chartData.values = dataList
                    data.notifyDataChanged()
                    notifyDataSetChanged()
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun setUpPrimaryUi(forecastDay: Forecastday) {

        setUpPrimarySwipeRefreshLayout()

        setUpPrimaryRecyclerView()

        setUpPrimaryCharts(forecastDay)

    }

    private fun setUpPrimarySwipeRefreshLayout() {
        requireView().forecastDetailsSwipeRefreshLayout.apply {
            setColorSchemeColors(Color.BLUE, Color.GREEN, Color.BLACK, Color.RED)
            Observable.create<Boolean> { emitter ->
                setOnRefreshListener {
                    isRefreshing = false
                    emitter.onNext(true)
                }
            }.throttleFirst(5000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable.add(d)
                    }

                    override fun onNext(t: Boolean) {
                        isRefreshing = true
                        doApiCall()
                    }

                    override fun onError(e: Throwable) {
                    }

                    override fun onComplete() {
                    }

                })
        }
    }

    private fun setUpPrimaryRecyclerView() {
        requireView().apply {
            hourlyForecastRecyclerViewAdapter =
                TodaysForecastRecyclerViewAdapter(arrayListOf(), requireContext())
            hourlyForecastRecyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            hourlyForecastRecyclerView.adapter = hourlyForecastRecyclerViewAdapter
        }
    }

    private fun setUpPrimaryCharts(forecastDay: Forecastday) {
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

                val lineDataSet = LineDataSet(arrayListOf(), "میزان بارندگی")
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

                val lineDataSet = LineDataSet(arrayListOf(), "دما")
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

                val lineDataSet = LineDataSet(arrayListOf(), "سرعت باد")
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

    private fun doApiCall() {
        viewModel.getWeatherDataFromApi(requireContext())
    }

    private fun calculateCurrentData(listData: List<MainWeatherData>): MainWeatherData? {
        val location = requireArguments().getString("location")
        val date = requireArguments().getString("date")
        if (!location.isNullOrEmpty() && !date.isNullOrEmpty()) {
            listData.forEach { mainWeatherData ->
                if (mainWeatherData.location == location)
                    return mainWeatherData
            }
        }
        return null
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[CurrentFragmentViewModel::class.java]
    }

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
    }
}