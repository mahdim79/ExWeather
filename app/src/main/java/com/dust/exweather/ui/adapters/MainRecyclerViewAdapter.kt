package com.dust.exweather.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasses.forecastweather.WeatherForecast
import com.dust.exweather.model.dataclasses.historyweather.Day
import com.dust.exweather.model.dataclasses.historyweather.WeatherHistory
import com.dust.exweather.model.dataclasses.maindataclass.MainWeatherData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import kotlinx.android.synthetic.main.item_main_recyclerview.view.*

class MainRecyclerViewAdapter(private val context: Context) :
    RecyclerView.Adapter<MainRecyclerViewAdapter.MainViewHolder>() {

    private var listData = arrayListOf<DataWrapper<Any>>()
    private var progressMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_main_recyclerview, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        when (listData[position].data) {
            is CurrentData -> {
                val data = listData[position].data as CurrentData
                Glide.with(context).load(data.current!!.condition.icon)
                    .into(holder.itemStateImage)
                holder.dayOfWeekText.text = "امروز"
                holder.item_TempText.text = data.current.temp_c.toString()
                holder.minTempText.visibility = View.INVISIBLE
                holder.maxTempText.visibility = View.INVISIBLE
                holder.weatherStateText.text =
                    data.current.condition.text
                holder.windSpeedText.text = data.current.wind_kph.toString()
            }
            is Day -> {
                val data = listData[position].data as Day
                Glide.with(context).load(data.condition.icon).into(holder.itemStateImage)
                holder.dayOfWeekText.text = data.dayOfWeek
                holder.minTempText.text = data.mintemp_c.toString()
                holder.maxTempText.text = data.maxtemp_c.toString()
                holder.weatherStateText.text = data.condition.text
                holder.windSpeedText.text = data.maxwind_kph.toString()
            }
            is com.dust.exweather.model.dataclasses.forecastweather.Day -> {
                val data =
                    listData[position].data as com.dust.exweather.model.dataclasses.forecastweather.Day
                Glide.with(context).load(data.condition.icon).into(holder.itemStateImage)
                holder.dayOfWeekText.text = data.dayOfWeek
                holder.minTempText.text = data.mintemp_c.toString()
                holder.maxTempText.text = data.maxtemp_c.toString()
                holder.weatherStateText.text = data.condition.text
                holder.windSpeedText.text = data.maxwind_kph.toString()
            }
        }
        holder.item_progressBar_divider.isIndeterminate = progressMode
    }

    override fun getItemCount(): Int = listData.size

    @SuppressLint("NotifyDataSetChanged")
    fun setNewData(newData: List<DataWrapper<Any>>) {
        listData.clear()
        listData.addAll(newData)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setProgressMode(pm: Boolean) {
        progressMode = pm
        notifyDataSetChanged()
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val windSpeedText = itemView.item_windSpeedText
        val weatherStateText = itemView.item_weatherStateText
        val maxTempText = itemView.item_maxTempText
        val minTempText = itemView.item_minTempText
        val dayOfWeekText = itemView.item_dayOfWeekText
        val itemStateImage = itemView.itemStateImage
        val item_TempText = itemView.item_TempText
        val item_progressBar_divider = itemView.item_progressBar_divider
    }

}