package com.dust.exweather.ui.fragments.weatherfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.dust.exweather.R
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import com.dust.exweather.viewmodel.factories.HistoryFragmentViewModelFactory
import com.dust.exweather.viewmodel.fragments.HistoryFragmentViewModel
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class WeatherHistoryFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: HistoryFragmentViewModelFactory

    private lateinit var viewModel: HistoryFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        observeCacheData()
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[HistoryFragmentViewModel::class.java]
    }

    private fun observeCacheData() {
        viewModel.getLiveWeatherDataFromCache().observe(viewLifecycleOwner) { rawData ->
            rawData?.let {
                addHistoryDataToDataList(rawData)
                setUpPrimaryUi()
            }
        }
    }

    private fun setUpPrimaryUi() {
        
    }

    private fun addHistoryDataToDataList(rawData: List<WeatherEntity>) {
        val data = rawData.map { it.toDataClass() }
        data.forEach { mainWeatherData ->
            mainWeatherData.historyDetailsData?.let { weatherHistory ->
                viewModel.getHistoryDataList().add(weatherHistory)
            }
        }
    }
}