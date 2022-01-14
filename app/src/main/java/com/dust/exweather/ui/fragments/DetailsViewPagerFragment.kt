package com.dust.exweather.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.dust.exweather.R
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import com.dust.exweather.utils.UtilityFunctions
import kotlinx.android.synthetic.main.fragment_viewpager_details.view.*

class DetailsViewPagerFragment(
    private val dataList: LiveData<List<WeatherEntity>>,
    private val position: Int
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
                    weatherHumidityText.text = data.current.humidity.toString()
                    weatherIsDayText.text = if (data.current.is_day == 1) "روز" else "شب"
                    precipText.text = data.current.precip_mm.toString()
                    windSpeedText.text = data.current.wind_kph.toString()
                    weatherCityNameText.text = data.location!!.name
                    weatherTempText.text = "${data.current.temp_c}°C"
                    lastUpdateText.text = UtilityFunctions.calculateLastUpdateText(data.current.system_last_update_epoch)
                    airPressureText.text = data.current.pressure_mb.toString()
                }
            } catch (e: Exception) {
            }
        }
    }
}