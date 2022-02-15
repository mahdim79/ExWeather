package com.dust.exweather.ui.fragments.bottomsheetdialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.dust.exweather.R
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_units_settings.*

class UnitsBottomSheetDialog(private val sharedPreferencesManager: SharedPreferencesManager) :
    BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_units_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogRounded

    private fun setupViews() {
        setupSpinners()
        setupCurrentSettings()
        setupSpinnersOnItemSelectedListeners()
    }

    private fun setupSpinnersOnItemSelectedListeners() {
        precipitationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                sharedPreferencesManager.setWeatherUnit(
                    Constants.PRECIPITATION_UNIT,
                    if (p2 == 0) Constants.MM else Constants.IN
                )
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

        temperatureSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                sharedPreferencesManager.setWeatherUnit(
                    Constants.TEMPERATURE_UNIT,
                    if (p2 == 0) Constants.C_PERCENTAGE else Constants.F_PERCENTAGE
                )
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

        windSpeedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                sharedPreferencesManager.setWeatherUnit(
                    Constants.WIND_SPEED_UNIT,
                    if (p2 == 0) Constants.KPH else Constants.MPH
                )
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

        pressureSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                sharedPreferencesManager.setWeatherUnit(
                    Constants.PRESSURE_UNIT,
                    if (p2 == 0) Constants.IN else Constants.MB
                )
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

        visibilitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                sharedPreferencesManager.setWeatherUnit(
                    Constants.VISIBILITY_UNIT,
                    if (p2 == 0) Constants.KM else Constants.MILE
                )
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

    }

    private fun setupCurrentSettings() {
        requireView().apply {
            if (sharedPreferencesManager.getWeatherUnit(Constants.PRECIPITATION_UNIT) == Constants.MM)
                precipitationSpinner.setSelection(0)
            else
                precipitationSpinner.setSelection(1)

            if (sharedPreferencesManager.getWeatherUnit(Constants.TEMPERATURE_UNIT) == Constants.C_PERCENTAGE)
                temperatureSpinner.setSelection(0)
            else
                temperatureSpinner.setSelection(1)

            if (sharedPreferencesManager.getWeatherUnit(Constants.WIND_SPEED_UNIT) == Constants.KPH)
                windSpeedSpinner.setSelection(0)
            else
                windSpeedSpinner.setSelection(1)

            if (sharedPreferencesManager.getWeatherUnit(Constants.PRESSURE_UNIT) == Constants.IN)
                pressureSpinner.setSelection(0)
            else
                pressureSpinner.setSelection(1)

            if (sharedPreferencesManager.getWeatherUnit(Constants.VISIBILITY_UNIT) == Constants.KM)
                visibilitySpinner.setSelection(0)
            else
                visibilitySpinner.setSelection(1)

        }
    }

    private fun setupSpinners() {
        requireView().apply {
            precipitationSpinner.adapter =
                getSpinnerArrayAdapter(arrayOf(getString(R.string.milimeter), getString(R.string.inch)))
            temperatureSpinner.adapter =
                getSpinnerArrayAdapter(arrayOf(getString(R.string.cPercentage), getString(R.string.fPercentage)))
            windSpeedSpinner.adapter =
                getSpinnerArrayAdapter(arrayOf(getString(R.string.kph), getString(R.string.mph)))
            pressureSpinner.adapter =
                getSpinnerArrayAdapter(arrayOf(getString(R.string.paPerInch), getString(R.string.milibar)))
            visibilitySpinner.adapter =
                getSpinnerArrayAdapter(arrayOf(getString(R.string.km), getString(R.string.mile)))
        }
    }

    private fun getSpinnerArrayAdapter(array: Array<String>): ArrayAdapter<String> {
        return ArrayAdapter(
            requireContext(),
            R.layout.item_units_spinner,
            array
        )
    }
}