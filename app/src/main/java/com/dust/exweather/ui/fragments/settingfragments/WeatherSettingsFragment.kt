package com.dust.exweather.ui.fragments.settingfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.dust.exweather.R
import com.dust.exweather.ui.fragments.bottomsheetdialogs.LocationsBottomSheetDialog
import com.dust.exweather.viewmodel.factories.WeatherSettingsViewModelFactory
import com.dust.exweather.viewmodel.fragments.WeatherSettingsViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_weather_settings.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class WeatherSettingsFragment : DaggerFragment() {

    @Inject
    lateinit var weatherSettingsViewModelFactory: WeatherSettingsViewModelFactory

    private lateinit var viewModel: WeatherSettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_weather_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        setUpView()
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(
            this,
            weatherSettingsViewModelFactory
        )[WeatherSettingsViewModel::class.java]
    }

    private fun setUpView() {
        requireView().apply {
            locationsSettings.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    LocationsBottomSheetDialog(
                        locationList = viewModel.getLocationsData(),
                        onDefaultLocationChanged = { defaultLocation ->
                            viewModel.setDefaultLocation(defaultLocation)
                        },
                        onLocationRemoved = { latLong ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                viewModel.removeLocation(latLong)
                            }
                        },
                        onAddLocationButtonClicked = {
                            findNavController().navigate(R.id.action_weatherSettingsFragment_to_addLocationFragment)
                        })
                        .show(
                            childFragmentManager,
                            "LocationsBottomSheetDialog"
                        )
                }
            }

            UnitsSettings.setOnClickListener {

            }
        }
    }
}