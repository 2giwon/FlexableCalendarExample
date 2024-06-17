package com.egiwon.example.flexablecalendarexample

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.egiwon.example.flexablecalendarexample.databinding.ItemCalendarMonthBinding

class CalendarViewHolder(
    @LayoutRes private val layoutRes: Int,
    parent: ViewGroup
): RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
) {
    val binding: ItemCalendarMonthBinding = ItemCalendarMonthBinding.bind(itemView)


}
