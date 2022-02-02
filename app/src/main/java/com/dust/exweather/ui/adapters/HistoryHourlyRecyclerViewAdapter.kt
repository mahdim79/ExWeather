package com.dust.exweather.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dust.exweather.R
import com.dust.exweather.utils.UtilityFunctions
import kotlinx.android.synthetic.main.item_todays_hour_forecast.view.*
import java.util.*

class HistoryHourlyRecyclerViewAdapter(
    private val listData: ArrayList<com.dust.exweather.model.dataclasses.historyweather.Hour>,
    private val context: Context
) : RecyclerView.Adapter<HistoryHourlyRecyclerViewAdapter.MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_todays_hour_forecast, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val data = listData[position]
        holder.apply {
            isDayImage.setImageResource(if (data.is_day == 1) R.drawable.ic_day_light else R.drawable.ic_night)
            dateTextView.text = UtilityFunctions.calculateCurrentTimeByTimeEpoch(data.time_epoch)
            temperatureTextView.text = data.temp_c.toString()
            precipText.text =
                context.resources.getString(R.string.precipitationText, data.precip_mm.toString())
            humidityText.text =
                context.resources.getString(R.string.humidityText, data.humidity.toString())
            windSpeedText.text =
                context.resources.getString(R.string.windSpeedText, data.wind_kph.toString())
            airPressureText.text =
                context.resources.getString(R.string.airPressureText, data.precip_mm.toString())

            Glide.with(context).load(data.condition.icon.replace("//", ""))
                .into(weatherStateImage)
            if (data.chance_of_rain >= 50)
                chanceOfRainImageView.setImageResource(R.drawable.ic_rain)

            if (data.chance_of_snow >= 50)
                chanceOfSnowImageView.setImageResource(R.drawable.ic_snow)
        }
    }

    override fun getItemCount(): Int = listData.size

    @SuppressLint("NotifyDataSetChanged")
    fun setNewData(newDataList: List<com.dust.exweather.model.dataclasses.historyweather.Hour>) {
        listData.clear()
        listData.addAll(newDataList)
        notifyDataSetChanged()
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView = itemView.dateTextView
        val temperatureTextView = itemView.temperatureTextView
        val precipText = itemView.precipText
        val humidityText = itemView.humidityText
        val windSpeedText = itemView.windSpeedText
        val airPressureText = itemView.airPressureText
        val weatherStateImage = itemView.weatherStateImage
        val chanceOfRainImageView = itemView.chanceOfRainImageView
        val chanceOfSnowImageView = itemView.chanceOfSnowImageView
        val isDayImage = itemView.isDayImage
    }
}