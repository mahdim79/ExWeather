package com.dust.exweather.ui.fragments.weatherfragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.model.room.CityDao
import com.dust.exweather.model.room.CurrentWeatherEntity
import com.dust.exweather.model.toDataClass
import com.dust.exweather.utils.Constants
import com.dust.exweather.utils.DataStatus
import com.dust.exweather.viewmodel.factories.CurrentFragmentViewModelFactory
import com.dust.exweather.viewmodel.fragments.CurrentFragmentViewModel
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_current_weather.view.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CurrentWeatherFragment : DaggerFragment() {

    private lateinit var viewModel: CurrentFragmentViewModel

    private val swipeRefreshLayoutDisposable = CompositeDisposable()

    // viewModel Factory Dependencies
    @Inject
    lateinit var repository: CurrentWeatherRepository

    @Inject
    lateinit var cityDao: CityDao

    @Inject
    lateinit var locationManager: LocationManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_current_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize View Model
        setUpViewModel()

        // set Up SwipeRefreshLayout
        setUpSwipeRefreshLayout()

        // observe Current Weather Livedata
        observeCurrentWeatherLiveData()

        // get current weather data
        getCurrentWeatherData()

    }

    private fun observeCurrentWeatherLiveData() {
        viewModel.getCurrentWeatherLiveData().observe(viewLifecycleOwner) { data ->
            prepareAndSetUpUi(dataFromApi = data, dataFromCache = null)
        }
    }

    @SuppressLint("CheckResult")
    private fun setUpSwipeRefreshLayout() {
        requireView().apply {

            // setup Swipe Refresh Colors
            swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.standardUiGreen
                ),
                ContextCompat.getColor(requireContext(), R.color.standardUiGreen),
                ContextCompat.getColor(requireContext(), R.color.standardUiBlue),
                ContextCompat.getColor(requireContext(), R.color.standardUiPink)
            )

            // setup SwipeRefreshLayout OnRefreshListener(anti refresh spam using rx android)
            Observable.create(ObservableOnSubscribe<Boolean> { emitter ->
                swipeRefreshLayout.setOnRefreshListener {
                    swipeRefreshLayout.isRefreshing = false
                    emitter.onNext(true)
                }
            })
                .throttleFirst(10000L, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {
                        swipeRefreshLayoutDisposable.add(d)
                    }

                    override fun onNext(t: Boolean) {
                        if (t) {
                            swipeRefreshLayout.isRefreshing = true
                            getCurrentWeatherData()
                        }
                    }

                    override fun onError(e: Throwable) {}

                    override fun onComplete() {}

                })

        }
    }

    private fun getCurrentWeatherData() {

        // check permissions
        if (!checkPermissionsGranted()) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 101
            )
            return
        }

        // get Cached Data From Room
        val roomLiveData = viewModel.getCurrentWeatherDataFromCache()
        roomLiveData.observe(viewLifecycleOwner) {
            roomLiveData.removeObservers(viewLifecycleOwner)
            prepareAndSetUpUi(dataFromApi = null, dataFromCache = it)

            // get Data From Server
            // * By Location
            viewModel.getCurrentWeatherDataByUserLocation(requireContext())

            // * By City Name
            //   viewModel.getCurrentWeatherByCityName(requireContext())


        }
    }

    private fun checkPermissionsGranted(): Boolean {
        return checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun prepareAndSetUpUi(
        dataFromCache: List<CurrentWeatherEntity>?,
        dataFromApi: DataWrapper<CurrentData>?
    ) {
        // check if both are null
        if (dataFromApi == null && dataFromCache == null) {
            resetSwipeRefreshLayout()
            return
        }

        // check if data fetched from server
        if (dataFromApi != null) {
            // fun if data is from api
            when (dataFromApi.status) {
                DataStatus.DATA_RECEIVE_SUCCESS -> {
                    setUpCurrentWeatherUi(dataFromApi.data!!)
                    resetSwipeRefreshLayout()
                }
                DataStatus.DATA_RECEIVE_FAILURE -> {
                    Toast.makeText(
                        requireContext(),
                        "مشکلی پیش آمده است اشکال: ${dataFromApi.data?.error ?: "unknown!"}",
                        Toast.LENGTH_SHORT
                    ).show()
                    resetSwipeRefreshLayout()
                }
                DataStatus.DATA_RECEIVE_LOADING -> {
                }
            }
        } else {
            // run if data is from cache
            if (!dataFromCache!!.isNullOrEmpty()) {
                setUpCurrentWeatherUi(dataFromCache[0].toDataClass())
            }
        }
    }

    private fun setUpCurrentWeatherUi(data: CurrentData) {
        setUpWeatherImageAndText(data.current!!.condition.code)
        setUpWeatherDetailsTexts(data)
    }

    private fun resetSwipeRefreshLayout() {
        requireView().swipeRefreshLayout.isRefreshing = false
    }

    private fun setUpWeatherDetailsTexts(current: CurrentData) {
        requireView().apply {
            weatherTempText.text = current.current!!.temp_c.toString()
            weatherCityNameText.text = current.location!!.name
            lastUpdateText.text = current.current.last_updated
            airPressureText.text = current.current.pressure_mb.toString()
            weatherHumidityText.text = current.current.humidity.toString()
            weatherIsDayText.text = current.current.is_day.toString()
            precipText.text = current.current.precip_mm.toString()
            windSpeedText.text = current.current.wind_kph.toString()

        }
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(
            this,
            CurrentFragmentViewModelFactory(
                requireActivity().application,
                repository,
                cityDao,
                locationManager
            )
        )[CurrentFragmentViewModel::class.java]
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.contains(-1))
                return
            getCurrentWeatherData()
        }
    }

    private fun setUpWeatherImageAndText(weatherCode: Int) {
        var imageUrl = Constants.DEFAULT_WALLPAPER_URL
        var weatherState = "مشخص نشده!"

        when (weatherCode) {
            0 -> {
                imageUrl = Constants.RAINY_WEATHER_WALLPAPER_URL
            }
        }

        requireView().weatherStateText.text = weatherState
        Glide.with(requireActivity().applicationContext).load(imageUrl)
            .into(requireView().weatherStateImage)
    }

    override fun onDestroyView() {
        swipeRefreshLayoutDisposable.clear()
        super.onDestroyView()
    }
}