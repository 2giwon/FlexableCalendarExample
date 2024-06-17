package com.egiwon.example.flexablecalendarexample

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.egiwon.example.flexablecalendarexample.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val onScrolled: (Calendar) -> Unit = { calendar ->
        val currentMonthText =
            "${calendar.get(Calendar.YEAR)}년 ${calendar.get(Calendar.MONTH) + 1}월"
        binding.tvMonth.text = currentMonthText
    }

    private var isScrollVertical = false
    private var isScrollHorizontal = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        makeCalendarRecyclerView()
        makeCalendarEvent()
    }

    private fun makeCalendarRecyclerView() {
        val linearLayoutManager = object :
            LinearLayoutManager(this, HORIZONTAL, false) {
            override fun canScrollHorizontally(): Boolean {
                return isScrollHorizontal
            }

            override fun canScrollVertically(): Boolean {
                return isScrollVertical
            }
        }

        binding.rvCalendar.layoutManager = linearLayoutManager
        binding.rvCalendar.setHasFixedSize(true)

        val calendarGesture = CalendarGestureDetector(
            binding.rvCalendar,
            onVerticalScrolled = {
                isScrollVertical = it
            },
            onHorizontalScrolled = {
                isScrollHorizontal = it
            }
        )
        calendarGesture.attachToRecyclerView()

        binding.rvCalendar.setOnTouchListener { v, event ->
            calendarGesture.onTouch(v, event)
            calendarGesture.handleTouchEvent(event)

            false
        }

        val calendarList = initCalendarList()
        val adapter = CalendarAdapter(R.layout.item_calendar_month)
        binding.rvCalendar.adapter = adapter
        adapter.updateItems(calendarList)

        binding.rvCalendar.scrollToPosition(100)
        val currentMonthText =
            "${calendarList[100].get(Calendar.YEAR)}년 ${calendarList[100].get(Calendar.MONTH) + 1}월"

        binding.tvMonth.text = currentMonthText
        binding.rvCalendar.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    onScrolled(calendarList[linearLayoutManager.findFirstVisibleItemPosition()])
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

            }
        })
    }

    private fun initCalendarList(): List<Calendar> {
        val calendarList = mutableListOf<Calendar>()
        for (i in -100..100) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, i)
            calendarList.add(calendar)
        }

        return calendarList
    }

    private fun makeCalendarEvent() {
        val calendarEventList = mutableListOf(
            CalendarEvent("Event is 1"),
            CalendarEvent("Event is 2"),
            CalendarEvent("Event is 3"),
            CalendarEvent("Event is 4"),
            CalendarEvent("Event is 5"),
        )

        val dailyScheduleAdapter = CalendarEventAdapter(R.layout.item_schedule)
        binding.rvCalendarEvent.adapter = dailyScheduleAdapter
        binding.rvCalendar.setHasFixedSize(true)

        dailyScheduleAdapter.updateItems(calendarEventList)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
