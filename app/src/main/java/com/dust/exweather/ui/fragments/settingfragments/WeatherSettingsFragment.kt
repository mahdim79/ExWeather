package com.dust.exweather.ui.fragments.settingfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.dust.exweather.R
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.ui.fragments.bottomsheetdialogs.AddCurrentLocationBottomSheetDialog
import com.dust.exweather.ui.fragments.bottomsheetdialogs.LocationsBottomSheetDialog
import com.dust.exweather.ui.fragments.bottomsheetdialogs.UnitsBottomSheetDialog
import com.dust.exweather.viewmodel.factories.WeatherSettingsViewModelFactory
import com.dust.exweather.viewmodel.fragments.WeatherSettingsViewModel
import com.dust.exweather.widget.WidgetUpdater
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_weather_settings.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WeatherSettingsFragment : DaggerFragment() {

    @Inject
    lateinit var weatherSettingsViewModelFactory: WeatherSettingsViewModelFactory

    @Inject
    lateinit var inputMethodManager: InputMethodManager

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    @Inject
    lateinit var widgetUpdater: WidgetUpdater

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
        hideKeyboard()
        setUpView()
    }

    private fun hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(
            requireView().locationsSettings.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
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
                            widgetUpdater.updateWidget()
                        },
                        onLocationRemoved = { latLong, defLocation ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                viewModel.removeLocation(latLong, requireContext())
                                defLocation?.let {
                                    viewModel.setDefaultLocation(it)
                                    widgetUpdater.updateWidget()
                                }
                            }
                        },
                        onAddLocationButtonClicked = {
                            findNavController().navigate(R.id.action_weatherSettingsFragment_to_addLocationFragment)
                        }, onAddCurrentLocationButtonClicked = {
                            AddCurrentLocationBottomSheetDialog(
                                onRequestCurrentLocation = {
                                    val result = viewModel.getCurrentUserLocation(requireContext())
                                    if (result == null) {
                                        true
                                    } else {
                                        Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT)
                                            .show()
                                        false
                                    }
                                }, onLocationAddButtonClicked = { location, name ->
                                    location?.let { latLng ->
                                        name?.let { strName ->
                                            lifecycleScope.launch(Dispatchers.IO) {
                                                val result =
                                                    viewModel.insertLocationToCache(latLng, strName, requireContext())
                                                withContext(Dispatchers.Main) {
                                                    if (result.isEmpty()) {
                                                        Toast.makeText(
                                                            requireContext(),
                                                            requireContext().getString(R.string.addedSuccessfully),
                                                            Toast.LENGTH_SHORT
                                                        )
                                                            .show()
                                                    } else {
                                                        Toast.makeText(
                                                            requireContext(),
                                                            result,
                                                            Toast.LENGTH_SHORT
                                                        )
                                                            .show()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }, locationDetailsLiveData = viewModel.getLocationDetailsLiveData()
                            ).show(
                                childFragmentManager,
                                "AddCurrentLocationBottomSheetDialog"
                            )
                        })
                        .show(
                            childFragmentManager,
                            "LocationsBottomSheetDialog"
                        )
                }
            }

            UnitsSettings.setOnClickListener {
                UnitsBottomSheetDialog(sharedPreferencesManager).show(
                    childFragmentManager,
                    "UnitsBottomSheetDialog"
                )
            }
        }
    }
}