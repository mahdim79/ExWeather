package com.dust.exweather.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.forecastweather.Hour
import com.dust.exweather.utils.UtilityFunctions
import com.dust.exweather.utils.customviews.CTextView
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
            context.resources.getString(R.string.precipStatus, data.precip_mm.toString())
        Glide.with(context).load(data.condition.icon.replace("//", ""))
            .into(holder.weatherStateImage)
        if (data.chance_of_rain >= 50)
            holder.chanceOfRainImageView.setImageResource(R.drawable.ic_cloud)

        if (data.chance_of_snow >= 50)
            holder.chanceOfSnowImageView.setImageResource(R.drawable.ic_cloud)
    }

    override fun getItemCount(): Int = listData.size

    @SuppressLint("NotifyDataSetChanged")
    fun setNewData(newDataList: List<Hour>) {
        listData.clear()
        listData.addAll(newDataList)
        notifyDataSetChanged()
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView = itemView.findViewById<TextView>(R.id.dateTextView)
        val temperatureTextView = itemView.findViewById<TextView>(R.id.temperatureTextView)
        val precipText = itemView.findViewById<CTextView>(R.id.precipText)
        val weatherStateImage = itemView.findViewById<ImageView>(R.id.weatherStateImage)
        val chanceOfRainImageView = itemView.findViewById<ImageView>(R.id.chanceOfRainImageView)
        val chanceOfSnowImageView = itemView.findViewById<ImageView>(R.id.chanceOfSnowImageView)
    }

    companion object {
        val diffUtilsCallback = object : DiffUtil.ItemCallback<Hour>() {
            override fun areItemsTheSame(oldItem: Hour, newItem: Hour): Boolean =
                oldItem.time == newItem.time

            override fun areContentsTheSame(oldItem: Hour, newItem: Hour): Boolean =
                oldItem.time == newItem.time

        }
    }
}