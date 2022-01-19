package com.dust.exweather.ui.fragments.others

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import com.dust.exweather.ui.adapters.DayDetailsViewPagerAdapter
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
import kotlinx.android.synthetic.main.fragment_weather_details.view.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WeatherDetailsFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: CurrentFragmentViewModelFactory

    private lateinit var viewModel: CurrentFragmentViewModel

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
            ViewModelProvider(this, viewModelFactory)[CurrentFragmentViewModel::class.java]
    }

    private fun setUpPrimaryUi() {
        setUpPrimaryViewPager()
        setUpSwipeRefreshLayout()
        viewModel.getLiveWeatherDataFromCache().observe(viewLifecycleOwner) {
            calculateAndSetUpUi(it)
        }
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
                    emitter.onNext(true)
                }
            }).throttleFirst(5000, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable.add(d)
                    }

                    override fun onNext(t: Boolean) {
                        viewModel.getWeatherDataFromApi(requireContext())
                    }

                    override fun onError(e: Throwable) {
                    }

                    override fun onComplete() {
                    }

                })
        }
    }

    private fun setUpPrimaryViewPager() {
        requireView().apply {
            weatherDetailsViewPager.adapter = DayDetailsViewPagerAdapter(
                childFragmentManager,
                viewModel.getLiveWeatherDataFromCache()
            )
            weatherDetailsViewPager.offscreenPageLimit = 2
            weatherDetailsTabLayout.setupWithViewPager(weatherDetailsViewPager)
        }
    }

    private fun calculateAndSetUpUi(data: List<WeatherEntity>) {
        var locationData: MainWeatherData? = null

        data.forEach {
            if (it.toDataClass().location == requireArguments().getString("location"))
                locationData = it.toDataClass()
        }

        if (locationData != null)
            setUpUi(locationData)

    }

    private fun setUpUi(data: MainWeatherData?) {
        // TODO: 1/18/2022 implement header ui
    }

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
    }
}