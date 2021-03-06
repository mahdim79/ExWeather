package com.dust.exweather.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.location.LocationData
import com.dust.exweather.utils.customviews.CTextView
import kotlinx.coroutines.*
import java.util.*

class LocationsRecyclerViewAdapter(
    private val context:Context,
    private val dataList: ArrayList<LocationData>,
    private val onDefaultLocationChanged: (String) -> Unit,
    private val onLocationRemoved: (String, String?) -> Unit

) :
    RecyclerView.Adapter<LocationsRecyclerViewAdapter.MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_location_recycler_view, parent, false)
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.apply {
            locationNameText.text = dataList[position].locationName
            if (dataList[position].default) {
                defaultRadioButton.isChecked = true
                defaultRadioButton.text = context.getString(R.string.defaultText)
            }else{
                defaultRadioButton.isChecked = false
                defaultRadioButton.text = ""
            }

            removeImage.setOnClickListener {
                if (dataList[position].redRemoveState) {
                    resetAllRemoveStates()

                    var defLocation:String? = null
                    val targetDeleteLocation:String = dataList[position].latlong

                    if (dataList[position].default){
                        if (dataList.size == 1){
                            defLocation = ""
                            dataList.removeAt(position)
                        } else{
                            dataList.removeAt(position)
                            defLocation = dataList[0].latlong
                            setNewDefaultLocation(0)
                        }
                    }else{
                        dataList.removeAt(position)
                    }

                    onLocationRemoved(targetDeleteLocation, defLocation)
                    notifyDataSetChanged()
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        withContext(Dispatchers.Main) {
                            try {
                                dataList[position].redRemoveState = true
                                removeImage.setImageResource(R.drawable.ic_baseline_remove_circle_24)
                            } catch (e: Exception) {
                            }
                        }
                        delay(2000)
                        withContext(Dispatchers.Main) {
                            try {
                                dataList[position].redRemoveState = false
                                removeImage.setImageResource(R.drawable.ic_baseline_remove_circle_yellow_24)
                            } catch (e: Exception) {
                            }
                        }
                    }
                }
            }

            defaultRadioButton.setOnClickListener {
                onDefaultLocationChanged(dataList[position].latlong)
                setNewDefaultLocation(position)
                notifyDataSetChanged()
            }

        }
    }

    private fun setNewDefaultLocation(position: Int) {
        for (i in dataList.indices)
            dataList[i].default = false
        dataList[position].default = true
    }

    private fun resetAllRemoveStates() {
        for (i in dataList.indices)
            dataList[i].redRemoveState = false
    }

    override fun getItemCount(): Int = dataList.size

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val locationNameText = itemView.findViewById<CTextView>(R.id.locationNameText)
        val defaultRadioButton = itemView.findViewById<RadioButton>(R.id.defaultRadioButton)
        val removeImage = itemView.findViewById<ImageView>(R.id.removeImage)
    }
}