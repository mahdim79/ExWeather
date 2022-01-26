package com.dust.exweather.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.forecastweather.Forecastday
import com.dust.exweather.utils.UtilityFunctions
import kotlinx.android.synthetic.main.item_main_forecast_recyclerview.view.*
import java.util.*

class ForecastMainRecyclerViewAdapter(
    private val listData: ArrayList<Forecastday>,
    private val context: Context
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
            dateTextView.text = context.resources.getString(
                R.string.dateText,
                currentData.day.dayOfWeek,
                UtilityFunctions.calculateCurrentDateByTimeEpoch(currentData.date_epoch)
            )
            minTempText.text = context.resources.getString(
                R.string.temperatureText,
                currentData.day.mintemp_c.toString()
            )
            maxTempText.text = context.resources.getString(
                R.string.temperatureText,
                currentData.day.maxtemp_c.toString()
            )
            Glide.with(context).load(currentData.day.condition.icon).into(weatherStateImage)
            weatherStateText.text = currentData.day.condition.text
            precipText.text = context.resources.getString(
                R.string.precipitationText,
                currentData.day.totalprecip_mm.toString()
            )
            weatherHumidityText.text = context.resources.getString(
                R.string.humidityText,
                currentData.day.avghumidity.toString()
            )
            averageTempTextView.text = context.resources.getString(
                R.string.temperatureText,
                currentData.day.avgtemp_c.toString()
            )
            visibilityTextView.text = context.resources.getString(
                R.string.visibilityText,
                currentData.day.avgvis_km.toString()
            )
            windSpeedRangeTextView.text = context.resources.getString(
                R.string.windSpeedText,
                currentData.day.maxwind_kph.toString()
            )
            uvIndexTextView.text = currentData.day.uv.toString()

            if (currentData.day.daily_chance_of_rain > 50)
                chanceOfRainImageView.visibility = View.VISIBLE
            if (currentData.day.daily_chance_of_snow > 50)
                chanceOfSnowImageView.visibility = View.VISIBLE

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