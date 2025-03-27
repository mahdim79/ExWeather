package com.dust.exweather.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.forecastweather.Forecastday
import com.dust.exweather.sharedpreferences.UnitManager
import com.dust.exweather.utils.UtilityFunctions
import kotlinx.android.synthetic.main.item_main_forecast_recyclerview.view.*
import java.util.*

class ForecastMainRecyclerViewAdapter(
    private val listData: ArrayList<Forecastday>,
    private val context: Context,
    private val navController: NavController,
    private val location: String,
    private val unitManager: UnitManager
) :
    RecyclerView.Adapter<ForecastMainRecyclerViewAdapter.MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_main_forecast_recyclerview, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val currentData = listData[position]
        holder.apply {
            dayOfWeekTextView.text = currentData.day.dayOfWeek

            dateTextView.text = UtilityFunctions.calculateCurrentDateByTimeEpoch(currentData.date_epoch)

            minTempText.text = unitManager.getTemperatureUnit(currentData.day.mintemp_c.toString(),currentData.day.mintemp_f.toString())

            maxTempText.text = unitManager.getTemperatureUnit(currentData.day.maxtemp_c.toString(),currentData.day.maxtemp_f.toString())

            UtilityFunctions.getWeatherIconResId(currentData.day.condition.icon,1, context)?.let { icon ->
                weatherStateImage.setImageResource(icon)
            }

            weatherStateText.text = currentData.day.condition.text

            precipText.text = unitManager.getPrecipitationUnit(currentData.day.totalprecip_mm.toString(),currentData.day.totalprecip_in.toString())

            weatherHumidityText.text = context.resources.getString(
                R.string.humidityText,
                currentData.day.avghumidity.toString()
            )
            averageTempTextView.text = unitManager.getTemperatureUnit(currentData.day.avgtemp_c.toString(),currentData.day.avgtemp_f.toString())

            visibilityTextView.text = unitManager.getVisibilityUnit(currentData.day.avgvis_km.toString(),currentData.day.avgvis_miles.toString())

            windSpeedRangeTextView.text = unitManager.getWindSpeedUnit(currentData.day.maxwind_kph.toString(),currentData.day.maxwind_mph.toString())

            uvIndexTextView.text = currentData.day.uv.toString()

            if (currentData.day.daily_chance_of_rain > 50)
                chanceOfRainImageView.visibility = View.VISIBLE
            if (currentData.day.daily_chance_of_snow > 50)
                chanceOfSnowImageView.visibility = View.VISIBLE

            itemView.setOnClickListener {
                val args = Bundle()
                args.apply {
                    putString("location", location)
                    putString("date", currentData.date)
                }
                navController.navigate(
                    R.id.forecastDetailsFragment,
                    args
                )
            }
        }
    }

    override fun getItemCount(): Int = listData.size

    @SuppressLint("NotifyDataSetChanged")
    fun setNewList(newList: List<Forecastday>) {
        listData.clear()
        listData.addAll(newList)
        notifyDataSetChanged()
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView = itemView.dateTextView
        val dayOfWeekTextView = itemView.dayOfWeekTextView
        val maxTempText = itemView.maxTempText
        val minTempText = itemView.minTempText
        val weatherStateImage = itemView.weatherStateImage
        val weatherStateText = itemView.weatherStateText
        val precipText = itemView.precipText
        val weatherHumidityText = itemView.weatherHumidityText
        val averageTempTextView = itemView.averageTempTextView
        val visibilityTextView = itemView.visibilityTextView
        val windSpeedRangeTextView = itemView.windSpeedRangeTextView
        val uvIndexTextView = itemView.uvIndexTextView
        val chanceOfSnowImageView = itemView.chanceOfSnowImageView
        val chanceOfRainImageView = itemView.chanceOfRainImageView
    }
}