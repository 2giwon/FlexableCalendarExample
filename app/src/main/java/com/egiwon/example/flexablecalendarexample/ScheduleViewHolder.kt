package com.egiwon.example.flexablecalendarexample

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.egiwon.example.flexablecalendarexample.databinding.ItemScheduleBinding

class ScheduleViewHolder(
    @LayoutRes val layoutResId: Int,
    parent: ViewGroup
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
) {
    private val binding: ItemScheduleBinding = ItemScheduleBinding.bind(itemView)

    fun bindData(calendarEvent: CalendarEvent) {
        with(binding) {
            tvSchedule.text = calendarEvent.title
        }
    }
}
