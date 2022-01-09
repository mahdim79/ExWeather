package com.dust.exweather.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.currentweather.main.CurrentData
import kotlinx.android.synthetic.main.item_main_recyclerview.view.*

class MainRecyclerViewAdapter : ListAdapter<CurrentData ,MainRecyclerViewAdapter.MainViewHolder>(diffCallBack) {

    val mainData = arrayListOf<CurrentData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_main_recyclerview , parent , false))
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
    }

    override fun getItemCount(): Int = mainData.size

    fun setNewList(newList:List<CurrentData>){
        mainData.clear()
        mainData.addAll(newList)
        submitList(mainData)
    }

    inner class MainViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val windSpeedText = itemView.item_windSpeedText
    }

    companion object{
        val diffCallBack = object :DiffUtil.ItemCallback<CurrentData>(){
            override fun areItemsTheSame(oldItem: CurrentData, newItem: CurrentData): Boolean {
                // TODO: 1/7/2022 calculate are items the same when you create dataclass
                return true
            }

            override fun areContentsTheSame(oldItem: CurrentData, newItem: CurrentData): Boolean {
                // TODO: 1/7/2022 calculate are items the same when you create dataclass
                return true
            }
        }
    }
}