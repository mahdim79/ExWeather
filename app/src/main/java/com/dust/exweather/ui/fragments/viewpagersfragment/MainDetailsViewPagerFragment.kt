package com.dust.exweather.ui.fragments.viewpagersfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.dust.exweather.R
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import com.dust.exweather.sharedpreferences.UnitManager
import com.dust.exweather.utils.UtilityFunctions
import kotlinx.android.synthetic.main.fragment_viewpager_details.view.*

class MainDetailsViewPagerFragment(
    private val dataList: LiveData<List<WeatherEntity>>,
    private val position: Int,
    private val progressLiveData: LiveData<Boolean>,
    private val navController: NavController,
    private val unitManager: UnitManager
) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_viewpager_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUi()
    }

    private fun setUpUi() {
        dataList.observe(viewLifecycleOwner) {
            try {
                val data = it[position].toDataClass().current!!
                requireView().apply {
                    weatherStateText.text = data.current!!.condition.text
                    weatherHumidityText.text = getString(R.string.humidityText, data.current.humidity.toString())
                    weatherIsDayText.text =
                        if (data.current.is_day == 1) getString(R.string.day) else getString(R.string.night)
                    precipText.text = unitManager.getPrecipitationUnit(data.current.precip_mm.toString(), data.current.precip_in.toString())
                    windSpeedText.text = unitManager.getWindSpeedUnit(data.current.wind_kph.toString(), data.current.wind_mph.toString())
                    weatherCityNameText.text = data.location!!.name
                    weatherTempText.text = unitManager.getTemperatureUnit(data.current.temp_c.toString(), data.current.temp_f.toString())
                    lastUpdateText.text =
                        UtilityFunctions.calculateLastUpdateText(
                            data.current.system_last_update_epoch,
                            requireContext()
                        )
                    airPressureText.text = unitManager.getPressureUnit(data.current.pressure_in.toString(), data.current.pressure_mb.toString())

                    detailsContainer.setOnClickListener { _ ->
                        val bundle = Bundle()
                        bundle.putString("location", it[position].toDataClass().location)
                        navController.navigate(
                            R.id.action_currentWeatherFragment_to_weatherDetailsFragment,
                            bundle
                        )
                    }

                }
            } catch (e: Exception) {
            }
        }

        progressLiveData.observe(viewLifecycleOwner) {
            requireView().apply {
                val progressBarMode = if (it) View.VISIBLE else View.INVISIBLE
                val viewMode = if (!it) View.VISIBLE else View.INVISIBLE
                progressBarNum1.visibility = progressBarMode
                progressBarNum2.visibility = progressBarMode
                progressBarNum3.visibility = progressBarMode
                progressBarNum4.visibility = progressBarMode
                progressBarNum5.visibility = progressBarMode
                progressBarNum6.visibility = progressBarMode
                progressBarNum7.visibility = progressBarMode
                progressBarNum8.visibility = progressBarMode
                progressBarNum9.visibility = progressBarMode

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
    }
}