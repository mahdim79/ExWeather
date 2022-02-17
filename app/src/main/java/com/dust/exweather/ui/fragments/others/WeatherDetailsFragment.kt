package com.dust.exweather.ui.fragments.others

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import com.dust.exweather.sharedpreferences.UnitManager
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
import kotlinx.android.synthetic.main.fragment_weather_details.view.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WeatherDetailsFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: CurrentFragmentViewModelFactory

    @Inject
    lateinit var alphaAnimation: AlphaAnimation

    @Inject
    lateinit var unitManager:UnitManager

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
        observeForApiCallState()
        setUpSwipeRefreshLayout()
        observeRoomData()
        viewModel.getWeatherDataFromApi(requireContext())
    }

    private fun observeRoomData() {
        viewModel.getLiveWeatherDataFromCache().observe(viewLifecycleOwner) {
            calculateAndSetUpUi(it)
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

    private fun calculateAndSetUpUi(data: List<WeatherEntity>) {
        data.forEach {
            if (it.toDataClass().location == requireArguments().getString("location"))
                setUpUi(it.toDataClass())
        }
    }

    private fun setUpUi(data: MainWeatherData) {

        requireView().apply {
            locationTextView.text = data.current?.location?.name ?: "null"
            weatherConditionTextView.text = data.current?.current?.condition?.text ?: "null"
            timeTextView.text = UtilityFunctions.calculateCurrentDateByTimeEpoch(
                data.current?.location?.localtime_epoch ?: 0,
                data.current?.location?.tz_id ?: "Asia/tehran"
            )
        }
    }

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
    }
}