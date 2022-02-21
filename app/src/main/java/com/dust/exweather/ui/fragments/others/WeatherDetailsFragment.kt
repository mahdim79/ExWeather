package com.dust.exweather.ui.fragments.others

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import com.dust.exweather.sharedpreferences.UnitManager
import com.dust.exweather.ui.adapters.ForecastMainRecyclerViewAdapter
import com.dust.exweather.ui.adapters.HistoryMainRecyclerViewAdapter
import com.dust.exweather.ui.anim.AnimationFactory
import com.dust.exweather.utils.DataStatus
import com.dust.exweather.utils.UtilityFunctions
import com.dust.exweather.viewmodel.factories.CurrentDetailsViewModelFactory
import com.dust.exweather.viewmodel.fragments.CurrentDetailsFragmentViewModel
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_forecast_details.view.*
import kotlinx.android.synthetic.main.fragment_weather_details.*
import kotlinx.android.synthetic.main.fragment_weather_details.view.*
import kotlinx.android.synthetic.main.fragment_weather_details.view.precipText
import kotlinx.android.synthetic.main.fragment_weather_details.view.visibilityTextView
import kotlinx.android.synthetic.main.fragment_weather_details.view.weatherHumidityText
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WeatherDetailsFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: CurrentDetailsViewModelFactory

    @Inject
    lateinit var unitManager: UnitManager

    @Inject
    lateinit var animationFactory: AnimationFactory

    private lateinit var forecastMainRecyclerViewAdapter: ForecastMainRecyclerViewAdapter

    private lateinit var historyMainRecyclerViewAdapter: HistoryMainRecyclerViewAdapter

    private lateinit var viewModel: CurrentDetailsFragmentViewModel

    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_weather_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        setUpPrimaryUi()
    }

    private fun setUpViewModel() {
        viewModel =
            ViewModelProvider(this, viewModelFactory)[CurrentDetailsFragmentViewModel::class.java]
    }

    private fun setUpPrimaryUi() {
        requireArguments().getString("location")?.let { location ->
            observeForApiCallState()
            setUpSwipeRefreshLayout()
            setupPrimaryRecyclerViews(location)
            observeRoomData(location)
            viewModel.getWeatherDataFromApi(requireContext())
        }
    }

    private fun setupPrimaryRecyclerViews(location: String) {
        forecastMainRecyclerViewAdapter = ForecastMainRecyclerViewAdapter(
            arrayListOf(),
            requireContext(),
            findNavController(),
            location,
            unitManager
        )

        historyMainRecyclerViewAdapter = HistoryMainRecyclerViewAdapter(
            arrayListOf(),
            requireContext(),
            unitManager,
            findNavController()
        )

        overallWeatherPredictionRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = forecastMainRecyclerViewAdapter
        }

        weatherHistoryRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = historyMainRecyclerViewAdapter
        }


    }

    private fun observeRoomData(location: String) {
        viewModel.getLiveWeatherDataFromCache().observe(viewLifecycleOwner) {
            calculateAndSetUpUi(it, location)
        }
    }

    private fun observeForApiCallState() {
        viewModel.getWeatherApiCallStateLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.DATA_RECEIVE_FAILURE -> {
                    resetSwipeRefreshLayout()
                    Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
                }

                DataStatus.DATA_RECEIVE_SUCCESS -> {
                    resetSwipeRefreshLayout()
                }
            }
        }
    }

    private fun resetSwipeRefreshLayout() {
        requireView().weatherDetailsSwipeRefreshLayout.isRefreshing = false
    }

    @SuppressLint("CheckResult")
    private fun setUpSwipeRefreshLayout() {
        requireView().weatherDetailsSwipeRefreshLayout.apply {
            setColorSchemeColors(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.standardUiBlue
                ),
                ContextCompat.getColor(requireContext(), R.color.standardUiGreen),
                ContextCompat.getColor(requireContext(), R.color.standardUiYellow)
            )

            // setup onRefresh Listener with anti spam system using rx android
            Observable.create(ObservableOnSubscribe<Boolean> { emitter ->
                setOnRefreshListener {
                    weatherDetailsSwipeRefreshLayout.isRefreshing = false
                    emitter.onNext(true)
                }
            }).throttleFirst(5000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable.add(d)
                    }

                    override fun onNext(t: Boolean) {
                        weatherDetailsSwipeRefreshLayout.isRefreshing = true
                        viewModel.getWeatherDataFromApi(requireContext())
                    }

                    override fun onError(e: Throwable) {
                    }

                    override fun onComplete() {
                    }

                })
        }
    }

    private fun calculateAndSetUpUi(data: List<WeatherEntity>, location: String) {
        data.forEach {
            if (it.toDataClass().location == location) {
                updateUi(it.toDataClass())
            }
        }
    }

    private fun updateUi(data: MainWeatherData) {
        requireView().apply {

            // update current location data
            data.current?.location?.let { location ->
                locationTextView.text = location.name
                timeTextView.text = UtilityFunctions.calculateCurrentTimeByTimeEpoch(
                    location.localtime_epoch,
                    location.tz_id
                )
                dateTextView.text = UtilityFunctions.calculateCurrentDateByTimeEpoch(
                    location.localtime_epoch,
                    location.tz_id
                ).plus(" ").plus(
                    UtilityFunctions.getDayOfWeekByUnixTimeStamp(
                        location.localtime_epoch,
                        requireContext()
                    )
                )
                countryTextView.text = location.country
                regionTextView.text = location.region
            }

            // update current status
            data.current?.current?.let { current ->
                weatherConditionTextView.text = current.condition.text

                UtilityFunctions.getWeatherIconResId(current.condition.icon,current.is_day, context)?.let { icon ->
                    weatherStateImageLocation.setImageResource(icon)
                }

                precipText.text = unitManager.getPrecipitationUnit(
                    current.precip_mm.toString(),
                    current.precip_in.toString()
                )
                weatherHumidityText.text =
                    getString(R.string.humidityText, current.humidity.toString())
                averageTempTextView.text = unitManager.getTemperatureUnit(
                    current.temp_c.toString(),
                    current.temp_f.toString()
                )
                visibilityTextView.text = unitManager.getVisibilityUnit(
                    current.vis_km.toString(),
                    current.vis_miles.toString()
                )
                windSpeedRangeTextView.text = unitManager.getWindSpeedUnit(
                    current.wind_kph.toString(),
                    current.wind_mph.toString()
                )

                airPressureText.text = unitManager.getPressureUnit(
                    current.pressure_in.toString(),
                    current.pressure_mb.toString()
                )

            }

            // update forecast weather recyclerView
            data.forecastDetailsData?.forecast?.forecastday?.let {
                forecastMainRecyclerViewAdapter.setNewList(it)
            }

            // update history weather recyclerView
            data.historyDetailsData?.let {
                historyMainRecyclerViewAdapter.apply {
                    setLocationAndLatLng(
                        it.location.name,
                        UtilityFunctions.createLatLongPattern(it.location)
                    )
                    setNewList(it.forecast.forecastday.reversed())
                }
            }

            // start animations
            ContainerView.visibility = View.VISIBLE
            ContainerView.startAnimation(animationFactory.getAlphaAnimation(0f, 1f, 1000))


        }
    }

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
    }
}