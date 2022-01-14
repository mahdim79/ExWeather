package com.dust.exweather.ui.fragments.weatherfragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.currentweather.other.WeatherStatesDetails
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.model.repositories.CurrentWeatherRepository
import com.dust.exweather.model.toDataClass
import com.dust.exweather.ui.adapters.DetailsViewPagerAdapter
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CurrentWeatherFragment : DaggerFragment() {

    private lateinit var viewModel: CurrentFragmentViewModel

    private val compositeDisposable = CompositeDisposable()

    // viewModel Factory Dependencies
    @Inject
    lateinit var repository: CurrentWeatherRepository

    @Inject
    lateinit var locationManager: LocationManager

    @Inject
    lateinit var alphaAnimation: AlphaAnimation

    private lateinit var mainRecyclerViewAdapter: MainRecyclerViewAdapter

    private lateinit var weatherStatesDetails: WeatherStatesDetails

    private var viewPagerPosition = 0

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

        addSomeLocations()

    }

    private fun addSomeLocations() {
        lifecycleScope.launch(Dispatchers.IO) {

            /*viewModel.insertLocationToCache("London")
            viewModel.insertLocationToCache("Tehran")*/

            withContext(Dispatchers.Main) {
                startAction()
            }
        }
    }

    private fun startAction() {
        observeForApiCallState()

        setUpUiView()

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

    private fun observeRecyclerViewLiveData() {
        viewModel.getLiveWeatherDataFromCache().observe(viewLifecycleOwner) {
            updateRecyclerViewContent(it[viewPagerPosition].toDataClass())
        }
    }

    private fun setUpUiView() {
        lifecycleScope.launch(Dispatchers.IO) {
            val listData = viewModel.getDirectWeatherDataFromCache()
            if (!listData.isNullOrEmpty())
                withContext(Dispatchers.Main) {
                    setUpPrimaryViewPager(listData.size)
                    setUpPrimaryRecyclerView(listData[0].toDataClass())
                    viewModel.getWeatherDataFromApi(requireContext())
                }
        }
    }

    private fun updateRecyclerViewContent(data: MainWeatherData) {
        if (data.current != null)
            mainRecyclerViewAdapter.setNewData(viewModel.calculateMainRecyclerViewDataList(data))
    }

    private fun setUpPrimaryRecyclerView(data: MainWeatherData) {
        mainWeatherRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val listData = arrayListOf<DataWrapper<Any>>()
        if (data.current != null)
            listData.addAll(viewModel.calculateMainRecyclerViewDataList(data))
        mainRecyclerViewAdapter =
            MainRecyclerViewAdapter(requireContext(), listData, alphaAnimation)
        mainWeatherRecyclerView.adapter = mainRecyclerViewAdapter
        observeRecyclerViewLiveData()

    }

    @SuppressLint("CheckResult")
    private fun setUpPrimaryViewPager(fragmentCount: Int) {
        detailsViewPager.adapter = DetailsViewPagerAdapter(
            childFragmentManager,
            viewModel.getLiveWeatherDataFromCache(),
            fragmentCount,
            viewModel.getDetailsViewPagerProgressStateLiveData()
        )
        detailsViewPager.offscreenPageLimit = fragmentCount - 1
        detailsViewPagerDotsIndicator.setViewPager(detailsViewPager)

        Observable.create(ObservableOnSubscribe<Int> { emitter ->
            detailsViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    emitter.onNext(position)
                }

                override fun onPageScrollStateChanged(state: Int) {}

            })
        }).debounce(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Int> {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onNext(position: Int) {
                    viewPagerPosition = position
                    lifecycleScope.launch(Dispatchers.IO) {
                        val data = viewModel.getDirectWeatherDataFromCache()
                        withContext(Dispatchers.Main) {
                            updateRecyclerViewContent(data[position].toDataClass())
                        }
                    }
                }

                override fun onError(e: Throwable) {}

                override fun onComplete() {}

            })
    }

    private fun instantiateWeatherStateDetailsObject() {
        weatherStatesDetails = UtilityFunctions.getWeatherStatesDetailsObject(requireContext())
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
                        compositeDisposable.add(d)
                    }

                    override fun onNext(t: Boolean) {
                        if (t) {
                            swipeRefreshLayout.isRefreshing = true
                            viewModel.getWeatherDataFromApi(requireContext())
                        }
                    }

                    override fun onError(e: Throwable) {}

                    override fun onComplete() {}

                })

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

    private fun setProgressMode(progressMode: Boolean) {
        mainRecyclerViewAdapter.setProgressMode(progressMode)
        viewModel.setDetailsViewPagerProgressState(progressMode)
    }

    private fun resetSwipeRefreshLayout() {
        requireView().swipeRefreshLayout.isRefreshing = false
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(
            this,
            CurrentFragmentViewModelFactory(
                requireActivity().application,
                repository,
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
            viewModel.getWeatherDataFromApi(requireContext())
        }
    }

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
    }
}