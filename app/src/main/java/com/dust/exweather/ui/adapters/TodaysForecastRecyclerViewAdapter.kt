package com.dust.exweather.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.forecastweather.Hour
import com.dust.exweather.utils.UtilityFunctions
import com.dust.exweather.utils.customviews.CTextView
import kotlinx.android.synthetic.main.item_todays_hour_forecast.view.*
import java.util.*

class TodaysForecastRecyclerViewAdapter(
    private val listData: ArrayList<Hour>,
    private val context: Context
) : RecyclerView.Adapter<TodaysForecastRecyclerViewAdapter.MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_todays_hour_forecast, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val data = listData[position]
        holder.dateTextView.text = UtilityFunctions.calculateCurrentTimeByTimeEpoch(data.time_epoch)
        holder.temperatureTextView.text = data.temp_c.toString()
        holder.precipText.text =
            context.resources.getString(R.string.precipitationText, data.precip_mm.toString())
        holder.humidityText.text =
            context.resources.getString(R.string.humidityText, data.humidity.toString())
        holder.windSpeedText.text =
            context.resources.getString(R.string.windSpeedText, data.wind_kph.toString())
        holder.airPressureText.text =
            context.resources.getString(R.string.airPressureText, data.precip_mm.toString())

        Glide.with(context).load(data.condition.icon.replace("//", ""))
            .into(holder.weatherStateImage)
        if (data.chance_of_rain >= 50)
            holder.chanceOfRainImageView.setImageResource(R.drawable.ic_rain)

        if (data.chance_of_snow >= 50)
            holder.chanceOfSnowImageView.setImageResource(R.drawable.ic_snow)
    }

    override fun getItemCount(): Int = listData.size

    @SuppressLint("NotifyDataSetChanged")
    fun setNewData(newDataList: List<Hour>) {
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
    }
}