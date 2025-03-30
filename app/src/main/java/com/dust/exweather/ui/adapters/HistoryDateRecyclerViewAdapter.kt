package com.dust.exweather.ui.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.dust.exweather.R
import com.dust.exweather.utils.customviews.CTextView
import com.google.android.material.card.MaterialCardView
import saman.zamani.persiandate.PersianDate

class HistoryDateRecyclerViewAdapter(private val context: Context) :
    ListAdapter<Pair<Int,Boolean>, HistoryDateRecyclerViewAdapter.MainViewHolder>(callback) {

    private lateinit var onItemSelectedListener: (Int,Boolean) -> Unit

    companion object {
        val callback = object : DiffUtil.ItemCallback<Pair<Int,Boolean>>() {
            override fun areItemsTheSame(oldItem: Pair<Int,Boolean>, newItem: Pair<Int,Boolean>): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Pair<Int,Boolean>, newItem: Pair<Int,Boolean>): Boolean {
                return oldItem == newItem
            }

        }
    }

    inner class MainViewHolder(itemView: View) : ViewHolder(itemView) {
        val tvDate = itemView.findViewById<CTextView>(R.id.tv_itemHistoryDate_date)
        val itemCard = itemView.findViewById<MaterialCardView>(R.id.cv_itemHistoryDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history_date_recyclerview, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val date = getItem(position)

        val persianDate = PersianDate(date.first * 1000L)

        holder.tvDate.text = "${persianDate.shYear}-${persianDate.shMonth}-${persianDate.shDay}"

        if (date.second){
            holder.itemCard.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context,R.color.mainContainerWhiteTransparentBackground))
        }else{
            holder.itemCard.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        }

        holder.itemView.setOnClickListener {
            if (::onItemSelectedListener.isInitialized)
                onItemSelectedListener.invoke(date.first,date.second)
        }
    }

    fun setOnItemSelectedListener(onItemSelectedListener: (Int,Boolean) -> Unit) {
        this.onItemSelectedListener = onItemSelectedListener
    }

}