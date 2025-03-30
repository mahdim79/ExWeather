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
import kotlinx.android.synthetic.main.fragment_weather_details.view.*

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
                    UtilityFunctions.getWeatherIconResId(data.current?.condition?.icon,data.current?.is_day, requireContext())?.let { icon ->
                        cloudImage.setImageResource(icon)
                    }

                    weatherStateText.text = UtilityFunctions.getConditionText(data.current!!.condition.text,data.current.condition.code,requireContext(),data.current.is_day == 1)
                    weatherIsDayText.text =
                        if (data.current.is_day == 1) getString(R.string.day) else getString(R.string.night)
                    weatherCityNameText.text = data.location!!.name
                    weatherTempText.text = unitManager.getTemperatureUnit(
                        data.current.temp_c.toString(),
                        data.current.temp_f.toString()
                    )
                    lastUpdateText.text =
                        UtilityFunctions.calculateLastUpdateText(
                            data.current.system_last_update_epoch,
                            requireContext()
                        )

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
                progressBarNum7.visibility = progressBarMode

                weatherTempText.visibility = viewMode
                weatherCityNameText.visibility = viewMode
                lastUpdateText.visibility = viewMode
                weatherIsDayText.visibility = viewMode
                weatherStateText.visibility = viewMode
            }
        }
    }
}