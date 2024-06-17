package com.egiwon.example.flexablecalendarexample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
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

    fun setWeek(week: Calendar) {
        weekStart = week
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val dayWidth = width / 7
        val week = weekStart.clone() as Calendar
        for (i in 0 until 7) {
            val dayText = week.get(Calendar.DAY_OF_MONTH).toString()
            val x = i * dayWidth + dayWidth / 2 - paint.measureText(dayText) / 2
            val y = 100f
            canvas.drawText(dayText, x, y, paint)
            week.add(Calendar.DATE, 1)
        }
    }
}
