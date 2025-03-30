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
import com.dust.exweather.model.dataclasses.currentweather.main.Condition
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import com.dust.exweather.model.dataclasses.historyweather.Forecastday
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.sharedpreferences.UnitManager
import com.dust.exweather.utils.UtilityFunctions
import kotlinx.android.synthetic.main.item_main_recyclerview.view.*
import okhttp3.internal.Util
import java.util.*

class MainRecyclerViewAdapter(
    private val context: Context,
    private var listData: ArrayList<DataWrapper<Any>>,
    private val alphaAnimation: AlphaAnimation,
    private val unitManager: UnitManager
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

                UtilityFunctions.getWeatherIconResId(data.current?.condition?.icon,1, context)?.let { icon ->
                    holder.itemStateImage.setImageResource(icon)
                }

                holder.dayOfWeekText.text = context.getString(R.string.today)
                holder.item_TempText.text = unitManager.getTemperatureUnit(data.current?.temp_c.toString(), data.current?.temp_f.toString())
                holder.minTempText.visibility = View.INVISIBLE
                holder.maxTempText.visibility = View.INVISIBLE
                holder.weatherStateText.text =
                    UtilityFunctions.getConditionText(data.current?.condition?.text!!,data.current.condition.code ,context)
                holder.dateText.text = UtilityFunctions.calculateCurrentDateByTimeEpoch(
                    data.location!!.localtime_epoch,
                    data.location.tz_id
                )
            }
            is Forecastday -> {
                val data = listData[position].data as Forecastday

                UtilityFunctions.getWeatherIconResId(data.day.condition.icon,1, context)?.let { icon ->
                    holder.itemStateImage.setImageResource(icon)
                }

                holder.dayOfWeekText.text = UtilityFunctions.getDayOfWeekByUnixTimeStamp(data.date_epoch, context)

                holder.minTempText.text = unitManager.getTemperatureUnit(data.day.mintemp_c.toString(), data.day.mintemp_f.toString())
                holder.maxTempText.text = unitManager.getTemperatureUnit(data.day.maxtemp_c.toString(), data.day.maxtemp_f.toString())
                holder.weatherStateText.text = UtilityFunctions.getConditionText(data.day.condition.text,data.day.condition.code ,context)
                holder.dateText.text =
                    UtilityFunctions.calculateCurrentDateByTimeEpoch(data.date_epoch)

            }
            is com.dust.exweather.model.dataclasses.forecastweather.Forecastday -> {
                val data =
                    listData[position].data as com.dust.exweather.model.dataclasses.forecastweather.Forecastday

                UtilityFunctions.getWeatherIconResId(data.day.condition.icon,1, context)?.let { icon ->
                    holder.itemStateImage.setImageResource(icon)
                }

                holder.dayOfWeekText.text = UtilityFunctions.getDayOfWeekByUnixTimeStamp(data.date_epoch, context)
                holder.minTempText.text = unitManager.getTemperatureUnit(data.day.mintemp_c.toString(), data.day.mintemp_f.toString())
                holder.maxTempText.text = unitManager.getTemperatureUnit(data.day.maxtemp_c.toString(), data.day.maxtemp_f.toString())
                holder.weatherStateText.text = UtilityFunctions.getConditionText(data.day.condition.text,data.day.condition.code,context)
                holder.dateText.text =
                    UtilityFunctions.calculateCurrentDateByTimeEpoch(data.date_epoch)
            }
        }
        holder.item_progressBar_divider.isIndeterminate = progressMode

        // setup Animation
        holder.itemView.startAnimation(alphaAnimation)

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