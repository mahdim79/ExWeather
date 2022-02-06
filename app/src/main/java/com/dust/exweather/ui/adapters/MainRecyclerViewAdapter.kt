package com.dust.exweather.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasses.historyweather.Forecastday
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.utils.UtilityFunctions
import kotlinx.android.synthetic.main.item_main_recyclerview.view.*
import java.util.*

class MainRecyclerViewAdapter(
    private val context: Context,
    private var listData: ArrayList<DataWrapper<Any>>,
    private val alphaAnimation: AlphaAnimation
) :
    RecyclerView.Adapter<MainRecyclerViewAdapter.MainViewHolder>() {

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
                Glide.with(context).load(data.current!!.condition.icon.replace("//", ""))
                    .into(holder.itemStateImage)
                holder.dayOfWeekText.text = context.getString(R.string.today)
                holder.item_TempText.text = data.current.temp_c.toString()
                holder.minTempText.visibility = View.INVISIBLE
                holder.maxTempText.visibility = View.INVISIBLE
                holder.weatherStateText.text =
                    data.current.condition.text
                holder.dateText.text = UtilityFunctions.calculateCurrentDateByTimeEpoch(
                    data.location!!.localtime_epoch,
                    data.location.tz_id
                )
            }
            is Forecastday -> {
                val data = listData[position].data as Forecastday
                Glide.with(context).load(data.day.condition.icon.replace("//", ""))
                    .into(holder.itemStateImage)
                holder.dayOfWeekText.text = data.day.dayOfWeek
                holder.minTempText.text = data.day.mintemp_c.toString()
                holder.maxTempText.text = data.day.maxtemp_c.toString()
                holder.weatherStateText.text = data.day.condition.text
                holder.dateText.text =
                    UtilityFunctions.calculateCurrentDateByTimeEpoch(data.date_epoch)
            }
            is com.dust.exweather.model.dataclasses.forecastweather.Forecastday -> {
                val data =
                    listData[position].data as com.dust.exweather.model.dataclasses.forecastweather.Forecastday
                Glide.with(context).load(data.day.condition.icon.replace("//", ""))
                    .into(holder.itemStateImage)
                holder.dayOfWeekText.text = data.day.dayOfWeek
                holder.minTempText.text = data.day.mintemp_c.toString()
                holder.maxTempText.text = data.day.maxtemp_c.toString()
                holder.weatherStateText.text = data.day.condition.text
                holder.dateText.text =
                    UtilityFunctions.calculateCurrentDateByTimeEpoch(data.date_epoch)
            }
        }
        holder.item_progressBar_divider.isIndeterminate = progressMode

        // setup Animation
        holder.itemView.startAnimation(alphaAnimation)

        if (position == listData.size - 1)
            holder.item_progressBar_divider.visibility = View.GONE

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
        val dateText = itemView.item_date
        val weatherStateText = itemView.item_weatherStateText
        val maxTempText = itemView.item_maxTempText
        val minTempText = itemView.item_minTempText
        val dayOfWeekText = itemView.item_dayOfWeekText
        val itemStateImage = itemView.itemStateImage
        val item_TempText = itemView.item_TempText
        val item_progressBar_divider = itemView.item_progressBar_divider
    }

}