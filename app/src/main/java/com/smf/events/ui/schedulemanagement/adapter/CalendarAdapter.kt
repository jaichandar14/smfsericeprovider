package com.smf.events.ui.schedulemanagement.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
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
    serviceDate: ArrayList<String>?,
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {
    private var days: ArrayList<LocalDate>? = null
    private var onItemListener: OnItemListener? = null
    var mViewDataBinding: FragmentCalendarBinding? = null
    var daytype: String? = null
    private var dayinWeek: ArrayList<String>? = null
    private var positonOfDays: ArrayList<Int>? = null
    var serviceDateList: ArrayList<String>? = null

    init {
        this.onItemListener = onItemListener
        this.days = day
        this.mViewDataBinding = mViewDataBinding
        this.daytype = type
        this.dayinWeek = dayinWeek
        this.positonOfDays = daysPositon
        this.serviceDateList = serviceDate
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.calendar_cell, parent, false)
        val layoutParams = view.layoutParams
        // 2528 This provie the gap between the days vertically
        if (days!!.size > 15) //month view
            layoutParams.height = (parent.height * 0.16).toInt() else  // week view
            layoutParams.height = (parent.height * 0.1).toInt()
        return CalendarViewHolder(view, onItemListener!!, days)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.calendarDWMLogics()
        if (daytype == "Week") {
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
            selectedWeekDates: ArrayList<LocalDate>,
        )

        // 2685  For Selection of Week
        fun weekSelection(
            pos: ArrayList<Int>,
            selectedDate: LocalDate?,
            absoluteAdapterPosition: Int,
        )
    }

    // 2458 Calendar View Holder Class
    inner class CalendarViewHolder(
        itemView: View,
        private val onItemListener: OnItemListener,
        days: ArrayList<LocalDate>?,
    ) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var days: ArrayList<LocalDate>?
        private val parentView: View = itemView.findViewById(R.id.parentView)
        private val dayOfMonth: TextView = itemView.findViewById(R.id.cellDayText)
        private var datadayMonth = ArrayList<String>()
        private var dayinWeek = ArrayList<String>()
        private val currentDateValue = LocalDateTime.now()
        private val formatterDay = DateTimeFormatter.ofPattern("dd")
        private val formatterMonth = DateTimeFormatter.ofPattern("MMM")
        private val formattedDay: String = currentDateValue.format(formatterDay)
        private val formattedMonth = currentDateValue.format(formatterMonth)
        private val thisMonth = YearMonth.now().format(formatterMonth)
        private var weekAbsPos: Int? = null
        private var postionOfDate = ArrayList<Int>()
        private var monthDate = ArrayList<LocalDate>()
        private var selectedWeekDates = ArrayList<LocalDate>()
        private var c: Calendar = Calendar.getInstance()
        private var cmonth = c.get(Calendar.MONTH)
        private var cDay = c.get(Calendar.DAY_OF_MONTH)

        init {
            itemView.setOnClickListener(this)
            this.days = days
        }

        // 2622 Calendar logics
        fun calendarDWMLogics() {
            var selectedDatePos = 0
            val date = days?.get(absoluteAdapterPosition)
            if (CalendarUtils.selectedDate == date) {
                selectedDatePos = absoluteAdapterPosition
            }
            dayOfMonth.text = date?.dayOfMonth.toString()
            // 2528 setting the color for present and previous month date
            if (daytype == "Day" || daytype == "Week" || daytype == "Month") {
                if (date?.month?.equals(CalendarUtils.selectedDate?.month)!!) {
                    dayOfMonth.setTextColor(Color.BLACK)
                    monthDate.add(date)
                    serviceDateList?.forEach {
                        val currentDayFormatter =
                            DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH)
                        val currentDay = LocalDate.parse(it, currentDayFormatter).dayOfMonth
                        val currentMonth = LocalDate.parse(it, currentDayFormatter)
                            .format(DateTimeFormatter.ofPattern("MMM"))
                        if (dayOfMonth.text == currentDay.toString() && mViewDataBinding?.monthYearTV?.text == currentMonth) {
                            dayOfMonth.setBackgroundResource(R.drawable.ic_checkbox_unchecked)
                        }
                    }
                    // 2622 Current Date Highlighter
                    selDateHighlight(date)
                    val c: Calendar = Calendar.getInstance()
                    val cmonth = c.get(Calendar.MONTH) + 1
                    if (date.monthValue < cmonth) {
                        dayOfMonth.setTextColor(Color.GRAY)
                    } else if (date.dayOfMonth < formattedDay.toInt() && mViewDataBinding?.monthYearTV?.text == formattedMonth) {
                        dayOfMonth.setTextColor(Color.GRAY)
                    }
                } else {
                    dayOfMonth.setTextColor(Color.LTGRAY)
                }

                if (CalendarUtils.selectedDate == days?.get(absoluteAdapterPosition)) {
                    weekAbsPos = absoluteAdapterPosition
                    val weekArrayDetails = daysInWeekArray(days?.get(weekAbsPos!!),
                        absoluteAdapterPosition)
                    weekArrayDetails.position.forEach {
                        postionOfDate.add(it)
                    }
                    onItemListener.weekSelection(postionOfDate.toSet().toList() as ArrayList<Int>,
                        CalendarUtils.selectedDate,
                        selectedDatePos)
                    postionOfDate.toSet().toList() as ArrayList<Int>
                }
            }
        }

        // 2528 For Selecting Entire Week
        fun weekSelection(previousDates: ArrayList<Int>?) {
            val date = days?.get(absoluteAdapterPosition)
            if (date?.monthValue!! > cmonth) {
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
        }

        // 2458 Clicked Date Method
        override fun onClick(view: View) {
            onClikedDates()
        }

        // 2458 Method To fetch the Week Of the day
        private fun daysInWeekArray(
            dayses: LocalDate?,
            absoluteAdapterPosition: Int,
        ): WeekArrayDetails {
            val days = ArrayList<LocalDate>()
            val weekdetails = sundayForDate(dayses!!, absoluteAdapterPosition)
            var current = weekdetails.date
            val currentPosition = weekdetails.position
            val endDate = current!!.plusWeeks(1)
            val weekPositions = ArrayList<Int>()
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
            var currentDates = current
            var absoluteAdapterPositionValue = absoluteAdapterPosition
            val oneWeekAgo = currentDates.minusWeeks(1)
            while (currentDates.isAfter(oneWeekAgo)) {
                if (currentDates.dayOfWeek == DayOfWeek.SUNDAY) {
                    return WeekDetails(currentDates, absoluteAdapterPositionValue)
                }
                currentDates = currentDates.minusDays(1)
                absoluteAdapterPositionValue = absoluteAdapterPositionValue.minus(1)
            }
            return WeekDetails(null, null)
        }

        // 2622 Current Date Highlighter
        private fun selDateHighlight(date: LocalDate) {
            if (daytype == "Day" || daytype == "Week") {
                // 2528 Setting and highlighting the current Date
                if (date == CalendarUtils.selectedDate && date.monthValue > cmonth) {
                    dayOfMonth.setTextColor(Color.WHITE)
                    dayOfMonth.setBackgroundResource(R.drawable.circle_fade_35)
                }
            }
        }

        private fun onClikedDates() {
            val date = days?.get(absoluteAdapterPosition)
            val weekArrayDetails =
                daysInWeekArray(days?.get(absoluteAdapterPosition), absoluteAdapterPosition)
            val datadayes: ArrayList<LocalDate> = weekArrayDetails.date
            val daysPositon = weekArrayDetails.position
            datadayes.forEach {
                selectedWeekDates.add(it)
                dayinWeek.add(it.dayOfMonth.toString())
                datadayMonth.add(it.month.toString())
            }
            // 2528 From Current Month Hiding the previous Month
            if (date?.monthValue!! > cmonth + 1) {
                onItemListener.onItemClick(absoluteAdapterPosition,
                    days?.get(absoluteAdapterPosition),
                    dayinWeek, daysPositon, selectedWeekDates)
            }
            // 2528  Current Date And Month Hiding
            if (mViewDataBinding?.monthYearTV?.text == thisMonth && date.dayOfMonth >= cDay) {
                onItemListener.onItemClick(absoluteAdapterPosition,
                    days?.get(absoluteAdapterPosition),
                    dayinWeek, daysPositon, selectedWeekDates)
            }
        }
    }
}