package com.dust.exweather.ui.fragments.weatherfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dust.exweather.R
import com.dust.exweather.model.toDataClass
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.sharedpreferences.UnitManager
import com.dust.exweather.ui.adapters.ForecastViewPagerAdapter
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
import kotlinx.android.synthetic.main.fragment_forecast_weather.*
import kotlinx.android.synthetic.main.fragment_forecast_weather.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WeatherForecastFragment : DaggerFragment() {

    private lateinit var viewModel: CurrentFragmentViewModel

    @Inject
    lateinit var viewModelFactory: CurrentFragmentViewModelFactory

    @Inject
    lateinit var unitManager :UnitManager

    @Inject
    lateinit var sharedPreferencesManager :SharedPreferencesManager

    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forecast_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        setUpUi()
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[CurrentFragmentViewModel::class.java]
    }

    private fun setUpUi() {
        observeForApiCallState()
        setUpPrimaryUi()
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

    private fun resetSwipeRefreshLayout() {
        requireView().forecastFragmentSwipeRefreshLayout.isRefreshing = false
    }

    private fun setProgressMode(progressMode: Boolean) {
        viewModel.setDetailsViewPagerProgressState(progressMode)
    }

    private fun setUpPrimaryUi() {
        lifecycleScope.launch(Dispatchers.IO) {
            val data = viewModel.getDirectWeatherDataFromCache().map { item -> item.toDataClass() }
            withContext(Dispatchers.Main) {
                requireView().forecastMainViewPager.apply {
                    adapter = ForecastViewPagerAdapter(
                        childFragmentManager,
                        viewModel.getLiveWeatherDataFromCache(),
                        data.size,
                        unitManager,
                        sharedPreferencesManager
                    )
                    offscreenPageLimit = data.size - 1
                }
                detailsViewPagerDotsIndicator.setViewPager(forecastMainViewPager)
            }
        }
        requireView().forecastFragmentSwipeRefreshLayout.apply {
            setColorSchemeColors(
                ContextCompat.getColor(requireContext(), R.color.standardUiYellow),
                ContextCompat.getColor(requireContext(), R.color.standardUiGreen),
                ContextCompat.getColor(requireContext(), R.color.standardUiBlue),
                ContextCompat.getColor(requireContext(), R.color.standardUiPink)
            )

            Observable.create(ObservableOnSubscribe<Boolean> { emitter ->
                setOnRefreshListener {
                    isRefreshing = false
                    emitter.onNext(true)
                }
            })
                .throttleFirst(5000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable.add(d)
                    }

                    override fun onNext(t: Boolean) {
                        doApiCall()
                    }

                    override fun onError(e: Throwable) {}

                    override fun onComplete() {}
                })
        }
    }

    private fun doApiCall() {
        viewModel.getWeatherDataFromApi(requireContext())
    }
}