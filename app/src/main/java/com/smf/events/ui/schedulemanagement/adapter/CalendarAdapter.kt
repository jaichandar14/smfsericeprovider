package com.smf.events.ui.schedulemanagement.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.R
import com.smf.events.databinding.FragmentCalendarBinding
import com.smf.events.helper.CalendarUtils
import java.time.DayOfWeek
import java.time.LocalDate

// 2458
class CalendarAdapter(
    day: java.util.ArrayList<LocalDate>?,
    onItemListener: OnItemListener,
    mViewDataBinding: FragmentCalendarBinding?,
    type: String,
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {
    var daysofweek: ArrayList<LocalDate>? = null
    var days: ArrayList<LocalDate>? = null
    var onItemListener: OnItemListener? = null
    var mViewDataBinding: FragmentCalendarBinding? = null
    var daytype: String? = null

    init {
        this.onItemListener = onItemListener
        this.days = day
        this.mViewDataBinding = mViewDataBinding
        this.daytype = type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.calendar_cell, parent, false)
        val layoutParams = view.layoutParams
        if (days!!.size > 15) //month view
            layoutParams.height = (parent.height * 0.17).toInt() else  // week view
            layoutParams.height = (parent.height * 0.1).toInt()
        return CalendarViewHolder(view, onItemListener!!, days)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        var date = days?.get(position)
        holder.dayOfMonth.text = date?.dayOfMonth.toString()
        if (date?.equals(CalendarUtils.selectedDate)!!)
            holder.parentView.setBackgroundResource(R.drawable.circle_fade)
        if (date.month.equals(CalendarUtils.selectedDate?.month)) {
            holder.dayOfMonth.setTextColor(Color.BLACK)
            if (holder.dayOfMonth.text == "29" && mViewDataBinding?.monthYearTV?.text == "Apr") {
                holder.parentView.setBackgroundResource(R.drawable.ic_checkbox_unchecked)
            }
        } else
            holder.dayOfMonth.setTextColor(Color.LTGRAY)
    }

    override fun getItemCount(): Int {
        return days!!.size
    }

    interface OnItemListener {
        fun onItemClick(position: Int, date: LocalDate?)
    }

    // 2458 Calendar View Holder Class
    inner class CalendarViewHolder(
        itemView: View,
        onItemListener: OnItemListener,
        days: ArrayList<LocalDate>?,
    ) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var days: ArrayList<LocalDate>?
        val parentView: View
        val dayOfMonth: TextView
        private val onItemListener: OnItemListener

        init {
            parentView = itemView.findViewById(R.id.parentView)
            dayOfMonth = itemView.findViewById(R.id.cellDayText)
            this.onItemListener = onItemListener
            itemView.setOnClickListener(this)
            this.days = days
            if (daytype == "week") {
                this.days = daysInWeekArray()
            }
        }

        // 2458 Clicked Date Method
        override fun onClick(view: View) {
            parentView.setBackgroundResource(R.drawable.circle_fade)
            daysofweek = daysInWeekArray()
            if (daytype == "Month") {
                parentView.setBackgroundResource(R.drawable.circle_fade)
            }
            onItemListener.onItemClick(absoluteAdapterPosition, days?.get(absoluteAdapterPosition))
        }
    }

    // 2458 Method To fetch the Week Of the day
    fun daysInWeekArray(): ArrayList<LocalDate>? {
        val days = ArrayList<LocalDate>()
        var current = CalendarUtils.selectedDate?.let { sundayForDate(it) }
        val endDate = current!!.plusWeeks(1)
        while (current!!.isBefore(endDate)) {
            days.add(current)
            current = current.plusDays(1)
        }
        return days
    }

    // Method to find the week Sunday
    private fun sundayForDate(current: LocalDate): LocalDate? {
        var current = current
        val oneWeekAgo = current.minusWeeks(1)
        while (current.isAfter(oneWeekAgo)) {
            if (current.dayOfWeek == DayOfWeek.SUNDAY) return current
            current = current.minusDays(1)
        }
        return null
    }
}