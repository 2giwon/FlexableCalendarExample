package com.egiwon.example.flexablecalendarexample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar

class WeekView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.BLACK
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }
    private var weekStart: Calendar = Calendar.getInstance()

    private var currentMonth: Int = weekStart.get(Calendar.MONTH)

    fun setWeek(week: Calendar) {
        weekStart = week
        invalidate()
    }

    fun setCurrentMonth(month: Int) {
        currentMonth = month
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val dayWidth = width / 7
        val week = weekStart.clone() as Calendar
        repeat(7) { i ->
            if (isInMonthDate(week.timeInMillis)) {
                paint.color = Color.BLACK
                paint.textSize = 60f
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
            } else {
                paint.color = Color.GRAY
                paint.textSize = 40f
            }
            val dayText = week.get(Calendar.DAY_OF_MONTH).toString()
            val x = i * dayWidth + dayWidth / 2 - paint.measureText(dayText) / 2
            val y = 100f
            canvas.drawText(dayText, x, y, paint)
            week.add(Calendar.DATE, 1)
        }
    }

    private fun isInMonthDate(checkEpochTime: Long): Boolean {
        val checkLocalDate = Instant.ofEpochMilli(checkEpochTime).atZone(ZoneId.systemDefault()).toLocalDate()

        return checkLocalDate.monthValue == currentMonth && checkLocalDate.year == weekStart.get(Calendar.YEAR)
    }
}
