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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.currentweather.main.Condition
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasses.currentweather.other.WeatherStatesDetails
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.model.room.CityDao
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import com.dust.exweather.ui.adapters.MainRecyclerViewAdapter
import com.dust.exweather.utils.DataStatus
import com.dust.exweather.utils.UtilityFunctions
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
import kotlinx.android.synthetic.main.fragment_current_weather.*
import kotlinx.android.synthetic.main.fragment_current_weather.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
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

    @Inject
    lateinit var mainRecyclerViewAdapter: MainRecyclerViewAdapter

    private lateinit var weatherStatesDetails: WeatherStatesDetails


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_current_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // instantiate Weather State Details Object
      //  instantiateWeatherStateDetailsObject()

        // initialize View Model
           setUpViewModel()

        // set Up SwipeRefreshLayout
           setUpSwipeRefreshLayout()

        // observe Weather Livedata
             observeWeatherLiveData()

        // get weather data
           getWeatherData()

        // setup Main RecyclerView
          setUpMainRecyclerView()

    }

    private fun instantiateWeatherStateDetailsObject() {
        weatherStatesDetails = UtilityFunctions.getWeatherStatesDetailsObject(requireContext())
    }

    private fun setUpMainRecyclerView() {
        mainWeatherRecyclerView.layoutManager = LinearLayoutManager(requireContext() , LinearLayoutManager.VERTICAL , false)
        mainWeatherRecyclerView.adapter = mainRecyclerViewAdapter
    }

    private fun observeWeatherLiveData() {
        viewModel.getWeatherLiveData().observe(viewLifecycleOwner) { data ->
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
                .throttleFirst(5000L, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {
                        swipeRefreshLayoutDisposable.add(d)
                    }

                    override fun onNext(t: Boolean) {
                        if (t) {
                            swipeRefreshLayout.isRefreshing = true
                            getWeatherData()
                        }
                    }

                    override fun onError(e: Throwable) {}

                    override fun onComplete() {}

                })

        }
    }

    private fun getWeatherData() {

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
        lifecycleScope.launch(Dispatchers.IO){
            val roomData = viewModel.getWeatherDataFromCache()
            withContext(Dispatchers.Main){
                prepareAndSetUpUi(dataFromApi = null, dataFromCache = roomData)

                // get Data From Server
                // * By Location
                viewModel.getWeatherDataByUserLocation(requireContext())

                // * By City Name
                 //  viewModel.getWeatherByCityName(requireContext())

            }
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
        dataFromCache: List<WeatherEntity>?,
        dataFromApi: DataWrapper<MainWeatherData>?
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
                    setProgressMode(false)
                    setUpUi(dataFromApi.data!!)
                    resetSwipeRefreshLayout()
                }
                DataStatus.DATA_RECEIVE_FAILURE -> {
                    setProgressMode(false)
                    Toast.makeText(
                        requireContext(),
                        "مشکلی پیش آمده است اشکال: ${dataFromApi.data!!.current!!.error ?: "unknown!"}",
                        Toast.LENGTH_SHORT
                    ).show()
                    resetSwipeRefreshLayout()
                }
                DataStatus.DATA_RECEIVE_LOADING -> {
                    setProgressMode(true)
                }
            }
        } else {
            // run if data is from cache
            if (!dataFromCache!!.isNullOrEmpty()) {
                setUpUi(dataFromCache[0].toDataClass())
            }
        }
    }

    private fun setProgressMode(progressMode:Boolean) {
        val progressBarMode = if (progressMode) View.VISIBLE else View.INVISIBLE
        val viewMode = if (!progressMode) View.VISIBLE else View.INVISIBLE
        requireView().apply {
            progressBarNum1.visibility = progressBarMode
            progressBarNum2.visibility = progressBarMode
            progressBarNum3.visibility = progressBarMode
            progressBarNum4.visibility = progressBarMode
            progressBarNum5.visibility = progressBarMode
            progressBarNum6.visibility = progressBarMode
            progressBarNum7.visibility = progressBarMode
            progressBarNum8.visibility = progressBarMode
            progressBarNum9.visibility = progressBarMode
            mainRecyclerViewAdapter.setProgressMode(progressMode)

            weatherTempText.visibility = viewMode
            weatherCityNameText.visibility = viewMode
            lastUpdateText.visibility = viewMode
            airPressureText.visibility = viewMode
            weatherHumidityText.visibility = viewMode
            weatherIsDayText.visibility = viewMode
            precipText.visibility = viewMode
            windSpeedText.visibility = viewMode
            weatherStateText.visibility = viewMode
        }
    }

    private fun setUpUi(data:MainWeatherData){
        setUpCurrentWeatherUi(data.current!!)
        setRecyclerViewList(data)
    }

    private fun setRecyclerViewList(data: MainWeatherData) {
        val newData = viewModel.calculateMainRecyclerViewDataList(data)
        mainRecyclerViewAdapter.setNewData(newData)
    }

    private fun setUpCurrentWeatherUi(data: CurrentData) {
        setUpWeatherImageAndText(data.current!!.condition)
        setUpWeatherDetailsTexts(data)
    }

    private fun resetSwipeRefreshLayout() {
        requireView().swipeRefreshLayout.isRefreshing = false
    }

    private fun setUpWeatherDetailsTexts(current: CurrentData) {
        requireView().apply {
            weatherTempText.text = current.current!!.temp_c.toString()
            weatherCityNameText.text = current.location!!.name
            lastUpdateText.text = viewModel.calculateLastUpdateText(current.current.system_last_update_epoch)
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
            getWeatherData()
        }
    }

    private fun setUpWeatherImageAndText(condition: Condition) {
        requireView().weatherStateText.text = condition.text
        /*Glide.with(requireActivity().applicationContext).load(UtilityFunctions.getWeatherStateGifUrl(weatherStatesDetails , condition.code))
            .into(requireView().weatherStateImage)*/
    }

    override fun onDestroyView() {
        swipeRefreshLayoutDisposable.clear()
        super.onDestroyView()
    }
}