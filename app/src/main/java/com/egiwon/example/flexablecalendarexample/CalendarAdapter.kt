package com.egiwon.example.flexablecalendarexample

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.egiwon.example.flexablecalendarexample.databinding.ItemCalendarMonthBinding
import java.util.Calendar

class CalendarAdapter(
    @LayoutRes private val layoutRes: Int,
): RecyclerView.Adapter<CalendarViewHolder>() {

    private val items = mutableListOf<Calendar>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        return CalendarViewHolder(layoutRes, parent)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val calendar = items[position]
        populateMonthView(calendar, holder.binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(items: List<Calendar>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    private fun populateMonthView(calendar: Calendar, binding: ItemCalendarMonthBinding) {
        binding.layoutMonth.removeAllViews()

        val current: Calendar = calendar.clone() as Calendar
        current.set(Calendar.DAY_OF_MONTH, 1)
        val firstOfWeek = current.get(Calendar.DAY_OF_WEEK) - 1
        current.add(Calendar.DAY_OF_MONTH, -firstOfWeek)

        for (i in 0 until 6) {
            val weekView = WeekView(binding.root.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1f
                )
            }
            val weekStart = current.clone() as Calendar
            weekView.setWeek(weekStart)
            binding.layoutMonth.addView(weekView)
            current.add(Calendar.DAY_OF_MONTH, 7)
        }
    }
}
