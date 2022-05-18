package com.smf.events.ui.schedulemanagement.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.R
import com.smf.events.databinding.FragmentCalendarBinding
import com.smf.events.helper.CalendarUtils
import com.smf.events.helper.WeekArrayDetails
import com.smf.events.helper.WeekDetails
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

// 2458
class CalendarAdapter(
    day: java.util.ArrayList<LocalDate>?,
    onItemListener: OnItemListener,
    mViewDataBinding: FragmentCalendarBinding?,
    type: String,
    dayinWeek: ArrayList<String>?,
    daysPositon: ArrayList<Int>?,
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {
    var days: ArrayList<LocalDate>? = null
    var onItemListener: OnItemListener? = null
    var mViewDataBinding: FragmentCalendarBinding? = null
    var daytype: String? = null
    var dayinWeek: ArrayList<String>? = null
    var positonOfDays: ArrayList<Int>? = null
    var previousDates: ArrayList<Int>? = null

    init {
        this.onItemListener = onItemListener
        this.days = day
        this.mViewDataBinding = mViewDataBinding
        this.daytype = type
        this.dayinWeek = dayinWeek
        this.positonOfDays = daysPositon
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.calendar_cell, parent, false)
        val layoutParams = view.layoutParams
        // 2528 This provie the gap between the days vertically
        if (days!!.size > 15) //month view
            layoutParams.height = (parent.height * 0.16).toInt() else  // week view
            layoutParams.height = (parent.height * 0.1).toInt()
        return CalendarViewHolder(view, onItemListener!!, days, daytype)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.calendarDWMLogics()
        Log.d("TAG", "onBindViewHoldernew: $positonOfDays")
        if (daytype=="Week"){
            //holder.autoSelectingWeek()
            //  holder.onClikedDates()
            holder.weekSelection(positonOfDays)
        }

    }

    override fun getItemCount(): Int {
        return days!!.size
    }

    interface OnItemListener {
        fun onItemClick(
            position: Int,
            date: LocalDate?,
            dayinWeek: ArrayList<String>,
            daysPositon: ArrayList<Int>,
        )
        fun weekSelection(
            pos: ArrayList<Int>,
            selectedDate: LocalDate?,
            absoluteAdapterPosition: Int
        )
    }

    // 2458 Calendar View Holder Class
    inner class CalendarViewHolder(
        itemView: View,
        onItemListener: OnItemListener,
        days: ArrayList<LocalDate>?,
        daytype: String?,
    ) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var days: ArrayList<LocalDate>?
        val parentView: View
        val dayOfMonth: TextView
        private val onItemListener: OnItemListener
        var datadayMonth = ArrayList<String>()
        var dayinWeek = ArrayList<String>()
        val currentDateValue = LocalDateTime.now()
        val formatterDay = DateTimeFormatter.ofPattern("dd")
        val formatterMonth = DateTimeFormatter.ofPattern("MMM")
        val formattedDay = currentDateValue.format(formatterDay)
        val formattedMonth = currentDateValue.format(formatterMonth)
        val thisMonth = YearMonth.now().format(formatterMonth)
        var weekAbsPos: Int? = null
        var postionOfDate = ArrayList<Int>()
        var newDate=ArrayList<Int>()
        init {
            parentView = itemView.findViewById(R.id.parentView)
            dayOfMonth = itemView.findViewById(R.id.cellDayText)
            this.onItemListener = onItemListener
            itemView.setOnClickListener(this)
            this.days = days
        }
        // 2622 Calendar logics
        fun calendarDWMLogics() {
            var selectedDatePos=0
            var date = days?.get(absoluteAdapterPosition)
            Log.d("TAG", "calendarDWMLogics: $date")
            if(CalendarUtils.selectedDate==date){
                selectedDatePos= absoluteAdapterPosition
            }
            dayOfMonth.text = date?.dayOfMonth.toString()
            // 2528 setting the color for present and previous month date
            if (daytype == "Day" || daytype == "Week" || daytype == "Month") {
                if (date?.month?.equals(CalendarUtils.selectedDate?.month)!!) {
                    dayOfMonth.setTextColor(Color.BLACK)
                    // 2622 Current Date Highlighter
                    selDateHighlight(date)
                    var c: Calendar = Calendar.getInstance()
                    var cmonth = c.get(Calendar.MONTH) + 1
                    if (date.monthValue < cmonth) {
                        dayOfMonth.setTextColor(Color.GRAY)
                    } else if (date.dayOfMonth < formattedDay.toInt() && mViewDataBinding?.monthYearTV?.text == formattedMonth) {
                        dayOfMonth.setTextColor(Color.GRAY)
                    }
                } else {
                    dayOfMonth.setTextColor(Color.LTGRAY)
                }

                if (CalendarUtils.selectedDate == days?.get(absoluteAdapterPosition)) {
                    Log.d("TAG", "onBindViewHolderWeek0: ${CalendarUtils.selectedDate} ")
                    Log.d("TAG", "onBindViewHolderWeek1: ${days?.get(absoluteAdapterPosition)} ")
//                holder.dayOfMonth.setTextColor(Color.WHITE)
//                holder.dayOfMonth.setBackgroundResource(R.drawable.circle_fade_35)
                    weekAbsPos = absoluteAdapterPosition
                    Log.d("TAG", "onBindViewHolderWeek12: ${weekAbsPos} ")
                    var weekArrayDetails = daysInWeekArray(days?.get(weekAbsPos!!),
                        absoluteAdapterPosition)
                    weekArrayDetails.position.forEach {
                        postionOfDate.add(it)
                    }
                    Log.d("TAG", "onBindViewHolderWeek122: ${postionOfDate.toSet()} ")
                    onItemListener.weekSelection(postionOfDate.toSet().toList() as ArrayList<Int>,CalendarUtils.selectedDate,selectedDatePos)
                    postionOfDate.toSet().toList() as ArrayList<Int>
//                Log.d("TAG", "onBindViewHolderWeek12: ${days?.get(holder.weekAbsPos!!)} ")
                }
            }
        }
        // 2528 For Selecting Entire Week
        fun weekSelection(previousDates: ArrayList<Int>?) {
            Log.d("TAG", "weekSelection: ${absoluteAdapterPosition}")
            if (absoluteAdapterPosition == previousDates?.first()) {
                parentView.setBackgroundResource(R.drawable.week_selector)
            } else if (absoluteAdapterPosition == previousDates?.last()) {
                parentView.setBackgroundResource(R.drawable.week_selection_right)
            }
            previousDates?.subList(1, 6)?.forEach {
                if (absoluteAdapterPosition == it) {
                    parentView.setBackgroundResource(R.drawable.week_selected_each)
                }
            }
        }


        // 2458 Clicked Date Method
        override fun onClick(view: View) {
            onClikedDates()
        }

        // 2458 Method To fetch the Week Of the day
        fun daysInWeekArray(dayses: LocalDate?, absoluteAdapterPosition: Int): WeekArrayDetails {
            val days = ArrayList<LocalDate>()
            var weekdetails = dayses.let { sundayForDate(it!!, absoluteAdapterPosition) }
            var current = weekdetails.date
            var currentPosition = weekdetails.position
            Log.d("TAG", "daysInWeekArray1: ${weekdetails.position}")
            val endDate = current!!.plusWeeks(1)
            var weekPositions = ArrayList<Int>()
            for (i in 0 until 7) {
                weekPositions.add(currentPosition!!.plus(i))
            }
            while (current!!.isBefore(endDate)) {
                days.add(current)
                current = current.plusDays(1)
            }
            return WeekArrayDetails(days, weekPositions)
        }

        // 2528 Method to find the week Sunday
        private fun sundayForDate(current: LocalDate, absoluteAdapterPosition: Int): WeekDetails {
            var current = current
            var absoluteAdapterPosition = absoluteAdapterPosition
            val oneWeekAgo = current.minusWeeks(1)
            while (current.isAfter(oneWeekAgo)) {
                if (current.dayOfWeek == DayOfWeek.SUNDAY) {
                    return WeekDetails(current, absoluteAdapterPosition)
                }
                current = current.minusDays(1)
                absoluteAdapterPosition = absoluteAdapterPosition.minus(1)
            }
            return WeekDetails(null, null)
        }

        // 2622 Current Date Highlighter
        fun selDateHighlight(date: LocalDate) {
            if (daytype == "Day" || daytype == "Week") {
                // 2528 Setting and highlighting the current Date
                if (date.equals(CalendarUtils.selectedDate)) {
                    dayOfMonth.setTextColor(Color.WHITE)
                    dayOfMonth.setBackgroundResource(R.drawable.circle_fade_35)
                }
            }
        }

        fun onClikedDates() {
            var date = days?.get(absoluteAdapterPosition)
            var weekArrayDetails =
                daysInWeekArray(days?.get(absoluteAdapterPosition), absoluteAdapterPosition)
            var datadayes = weekArrayDetails.date
            var daysPositon = weekArrayDetails.position
            datadayes.forEach {
                dayinWeek.add(it.dayOfMonth.toString())
                datadayMonth.add(it.month.toString())
            }
            var c: Calendar = Calendar.getInstance()
            var cmonth = c.get(Calendar.MONTH)
            var cDay = c.get(Calendar.DAY_OF_MONTH)
            // 2528 From Current Month Hiding the previous Month
            if (date?.monthValue!! > cmonth + 1) {
                onItemListener.onItemClick(absoluteAdapterPosition,
                    days?.get(absoluteAdapterPosition),
                    dayinWeek, daysPositon)
            }
            // 2528  Current Date And Month Hiding
            if (mViewDataBinding?.monthYearTV?.text == thisMonth && date.dayOfMonth >= cDay) {
                onItemListener.onItemClick(absoluteAdapterPosition,
                    days?.get(absoluteAdapterPosition),
                    dayinWeek, daysPositon)
            }
        }
    }
}