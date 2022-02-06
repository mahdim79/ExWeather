package com.dust.exweather.ui.fragments.viewpagersfragment.currentdetailsfragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.bumptech.glide.Glide
import com.dust.exweather.R
import com.dust.exweather.interfaces.DayDetailsViewPagerOnClickListener
import com.dust.exweather.model.dataclasses.currentweather.main.Current
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.room.WeatherEntity
import com.dust.exweather.model.toDataClass
import com.dust.exweather.utils.UtilityFunctions
import kotlinx.android.synthetic.main.fragment_current_details_viewpager.*
import kotlinx.android.synthetic.main.fragment_forecast_details_viewpager.view.*
import java.util.*

class CurrentDetailsViewPagerFragment(
    private val data: LiveData<List<WeatherEntity>>,
    private val alphaAnimation: AlphaAnimation,
    private val location: String,
    private val onClickListener: DayDetailsViewPagerOnClickListener
) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_current_details_viewpager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
        setUpOtherStuff()
    }

    private fun observeData() {
        data.observe(viewLifecycleOwner) {
            it.forEach {
                val tempData = it.toDataClass()
                if (tempData.location == location)
                    setUpUi(tempData)
            }
        }
    }

    private fun setUpOtherStuff() {
        requireView().apply {
            scrollHelperCardView.setOnClickListener {
                onClickListener.goToHistoryWeatherPage()
            }
            scrollHelperCardView2.setOnClickListener {
                onClickListener.goToForecastWeatherPage()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpUi(locationData: MainWeatherData) {
        requireView().apply {
            val currentData = locationData.current!!

            weatherStateText.text = currentData.current!!.condition.text

            temperatureTextView.text =
                "${String.format(Locale.ENGLISH, "%.0f", currentData.current.temp_c)}°C"

            windSpeedTextView.text =
                "${currentData.current.wind_kph} Kph | ${currentData.current.wind_dir}"

            localDateTextView.text = UtilityFunctions.calculateCurrentDateByTimeEpoch(
                currentData.location!!.localtime_epoch,
                currentData.location.tz_id
            )

            localTimeTextView.text = UtilityFunctions.calculateCurrentTimeByTimeEpoch(
                currentData.location.localtime_epoch,
                currentData.location.tz_id
            )

            Glide.with(requireContext()).load(currentData.current.condition.icon.replace("//", ""))
                .into(weatherStateImage)

            weatherDetailsTextView.text = createDetailsString(currentData.current)

        }
    }

    private fun createDetailsString(currentData: Current): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(
            "${
                requireActivity().resources.getString(
                    R.string.cloudStatus,
                    currentData.cloud.toString()
                )
            } \n" +
                    "${
                        requireActivity().resources.getString(
                            R.string.humidityStatus,
                            currentData.humidity.toString()
                        )
                    } \n" +
                    "${
                        requireActivity().resources.getString(
                            R.string.precipStatus,
                            currentData.precip_mm.toString()
                        )
                    } \n" +
                    "${
                        requireActivity().resources.getString(
                            R.string.airPressureStatus,
                            currentData.pressure_mb.toString()
                        )
                    } \n" +
                    "${
                        requireActivity().resources.getString(
                            R.string.uvStatus,
                            currentData.uv.toString()
                        )
                    } \n" +
                    "${
                        requireActivity().resources.getString(
                            R.string.visibilityStatus,
                            currentData.vis_km.toString()
                        )
                    } \n"
        )

        if (currentData.air_quality != null) {
            stringBuilder.append(
                "${
                    requireActivity().resources.getString(
                        R.string.so2Amount,
                        if (currentData.air_quality.so2 != null) currentData.air_quality.so2.toString() else ""
                    )
                } \n" +
                        "${
                            requireActivity().resources.getString(
                                R.string.coAmount,
                                (currentData.air_quality.co ?: getString(R.string.unknown)).toString()
                            )
                        } \n" +
                        "${
                            requireActivity().resources.getString(
                                R.string.o3Amount,
                                (currentData.air_quality.o3 ?: getString(R.string.unknown)).toString()
                            )
                        } \n" +
                        "${
                            requireActivity().resources.getString(
                                R.string.no2Amount,
                                (currentData.air_quality.no2 ?: getString(R.string.unknown)).toString()
                            )
                        } \n" +
                        "${
                            requireActivity().resources.getString(
                                R.string.defraIndex,
                                (currentData.air_quality.`gb-defra-index` ?: getString(R.string.unknown)).toString()
                            )
                        } \n" +
                        "${
                            requireActivity().resources.getString(
                                R.string.epaIndex,
                                (currentData.air_quality.`us-epa-index` ?: getString(R.string.unknown)).toString()
                            )
                        } \n"
            )
        }
        return stringBuilder.toString()
    }
}