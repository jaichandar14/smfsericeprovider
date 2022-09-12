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
import com.smf.events.helper.AppConstants
import com.smf.events.helper.CalendarUtils
import com.smf.events.helper.WeekArrayDetails
import com.smf.events.helper.WeekDetails
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

// 2458
class CalendarAdapter(
    day: java.util.ArrayList<LocalDate>?,
    onItemListener: OnItemListener,
    mViewDataBinding: FragmentCalendarBinding?,
    type: String,
    dayinWeek: ArrayList<String>?,
    daysPositon: ArrayList<Int>?,
    serviceDate: ArrayList<String>?,
    businessValidity: LocalDate?,
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {
    private var days: ArrayList<LocalDate>? = null
    private var onItemListener: OnItemListener? = null
    var mViewDataBinding: FragmentCalendarBinding? = null
    var daytype: String? = null
    private var dayinWeek: ArrayList<String>? = null
    private var positonOfDays: ArrayList<Int>? = null
    var serviceDateList: ArrayList<String>? = null
    var i = 1
    var weekMapListAll: HashMap<LocalDate, Int> = HashMap()
    var businessValidity: LocalDate? = null
    var onClickExpDate = 0

    init {
        this.onItemListener = onItemListener
        this.days = day
        this.mViewDataBinding = mViewDataBinding
        this.daytype = type
        this.dayinWeek = dayinWeek
        this.positonOfDays = daysPositon
        this.serviceDateList = serviceDate
        this.businessValidity = businessValidity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.calendar_cell, parent, false)
        val layoutParams = view.layoutParams
        // 2528 This provie the gap between the days vertically
        if (days!!.size > 15) //month view
            layoutParams.height = (parent.height * 0.16).toInt() else  // week view
            layoutParams.height = (parent.height * 0.1).toInt()
        return CalendarViewHolder(view, onItemListener!!, days, businessValidity)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {

        if (CalendarUtils.businessValidity != null) {
            Log.d("TAG", "onBindViewHolder: ${CalendarUtils.businessValidity}")
            holder.calendarDWMLogics(CalendarUtils.businessValidity)
        }
        if (daytype == "Week") {
            Log.d("TAG", "onBindViewHolder: $position")
            holder.weekSelection(positonOfDays, position)
        }
        // 2796 Method for fetching all Date nd its position
        fetchingAllDateAndPosition(position)
    }

    private fun fetchingAllDateAndPosition(position: Int) {
        val date = days?.get(position)
        date?.let { weekMapListAll.put(it, position) }
        if (position == 41) {
            onItemListener?.weekMapList(weekMapListAll)
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

        // 2796  For All Date and Position
        fun weekMapList(
            weekMapList: HashMap<LocalDate, Int>?,
        )

        fun onClickBusinessExpDate(
            valid: Boolean,
        )

    }

    // 2458 Calendar View Holder Class
    inner class CalendarViewHolder(
        itemView: View,
        private val onItemListener: OnItemListener,
        days: ArrayList<LocalDate>?,
        businessValidity: LocalDate?,
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
        private var cyear = c.get(Calendar.YEAR)

        init {
            itemView.setOnClickListener(this)
            this.days = days
        }

        // 2622 Calendar logics
        fun calendarDWMLogics(businessValidity: LocalDate?) {
            var selectedDatePos = 0
            val date = days?.get(absoluteAdapterPosition)
            Log.d("TAG", "calendarDWMLogicsDatte:$date ")

            if (CalendarUtils.selectedDate == date) {
                selectedDatePos = absoluteAdapterPosition
            }
            dayOfMonth.text = date?.dayOfMonth.toString()
            RxBus.publish(RxEvent.IsValid(true))
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
                        // 2743 Method for high-lighting the upcoming month
                        eventHighLighter(currentDay, currentMonth, date)

                    }
                    if (businessValidity != null) {
                        val formatter = DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT_MONTH)
                        date.format(formatter)
                        businessEndDateHighlighter(
                            businessValidity.dayOfMonth,
                            businessValidity.format(formatter),
                            businessValidity
                        )
                        if (date.equals(CalendarUtils.selectedDate)) {
                            onClickBusniessValidityDate()
                        }
                    }
                    // 2743 Method For hiding the previous DAY,MONTH,YEAR
                    var eventDate: LocalDate = LocalDate.of(2022, 8, 25)
                    previousDMYHider(date, businessValidity)
                    busniessValidation(businessValidity, date)
                } else {
                    dayOfMonth.setTextColor(Color.LTGRAY)
                }

                if (CalendarUtils.selectedDate == days?.get(absoluteAdapterPosition)) {
                    weekAbsPos = absoluteAdapterPosition
                    val weekArrayDetails = daysInWeekArray(
                        days?.get(weekAbsPos!!),
                        absoluteAdapterPosition
                    )
                    weekArrayDetails.position.forEach {
                        postionOfDate.add(it)
                    }
                    onItemListener.weekSelection(
                        postionOfDate.toSet().toList() as ArrayList<Int>,
                        CalendarUtils.selectedDate,
                        selectedDatePos
                    )
                    postionOfDate.toSet().toList() as ArrayList<Int>
                }
            }

            if (date == LocalDate.now()) {
                dayOfMonth.setTextColor(Color.BLACK)
                dayOfMonth.setBackgroundResource(R.drawable.current_date_circle)
            }

        }

        private fun onClickBusniessValidityDate() {
            onClickExpDate += 1
            if (CalendarUtils.selectedDate!! > businessValidity) {
                CalendarUtils.toastCount += 1
                Log.d("TAG", "toastCount: ${CalendarUtils.toastCount}")
                if (CalendarUtils.toastCount == 1) {
                    onItemListener.onClickBusinessExpDate(true)
                }
            }
            if (CalendarUtils.selectedDate!! == businessValidity)
                onItemListener.onClickBusinessExpDate(false)
        }

        private fun busniessValidation(date: LocalDate?, date1: LocalDate) {
            if (date != null) {
                Log.d("TAG", "busniessvalidation: $date $date1")
//                if (date.year < date1.year) {
//                    dayOfMonth.setTextColor(Color.GRAY)
//                    if (date.monthValue <= date1.monthValue&&date.year <= date1.year&& date.dayOfMonth < date1.dayOfMonth) {
//                        dayOfMonth.setTextColor(Color.GRAY)
//                        if (date.monthValue <= date1.monthValue && date.dayOfMonth < date1.dayOfMonth && date.year <= date1.year) {
//                            dayOfMonth.setTextColor(Color.GRAY)
//                        }
//                    }
//                }
                if (date < date1) {
                    dayOfMonth.setTextColor(Color.GRAY)
                }
            }
        }

        // 2743 Method For hiding the previous DAY,MONTH,YEAR
        private fun previousDMYHider(date: LocalDate, eventDate: LocalDate?) {
            val c: Calendar = Calendar.getInstance()
            val cmonth = c.get(Calendar.MONTH) + 1
            if (mViewDataBinding?.yearTV?.text.toString().toInt() >= cyear) {
                // 2622 Current Date Highlighter
                if (eventDate != null) {
                    selDateHighlight(date, eventDate)
                }
            }
            if (date.monthValue < cmonth && date.year == cyear) {
                dayOfMonth.setTextColor(Color.GRAY)
            } else if (date.dayOfMonth < formattedDay.toInt() && mViewDataBinding?.monthYearTV?.text == formattedMonth && mViewDataBinding?.yearTV?.text.toString()
                    .toInt() == cyear
            ) {
                dayOfMonth.setTextColor(Color.GRAY)
            } else if (mViewDataBinding?.yearTV?.text.toString().toInt() < cyear) {
                dayOfMonth.setTextColor(Color.GRAY)
            }
        }

        // 2743 Method HighLighting the events
        private fun eventHighLighter(currentDay: Int, currentMonth: String, date: LocalDate) {
            // 2735 If condition for filter the event for  upcoming Dates
            if (dayOfMonth.text.toString()
                    .toInt() == currentDay && mViewDataBinding?.monthYearTV?.text == currentMonth
            ) {
                // 2528 Setting and highlighting the current Date
                if (date.monthValue < cmonth + 1 && mViewDataBinding?.yearTV?.text.toString()
                        .toInt() == cyear
                ) {
                } else if (mViewDataBinding?.monthYearTV?.text == formattedMonth && mViewDataBinding?.yearTV?.text.toString()
                        .toInt() == cyear
                ) {
                    if (dayOfMonth.text.toString().toInt() >= LocalDate.now().dayOfMonth) {
                        dayOfMonth.setBackgroundResource(R.drawable.hallow_circle)
                    }
                } else {
                    dayOfMonth.setBackgroundResource(R.drawable.hallow_circle)
                }
            }
        }

        // 2743 Method HighLighting the events
        private fun businessEndDateHighlighter(
            currentDay: Int,
            currentMonth: String,
            date: LocalDate,
        ) {
            // 2735 If condition for filter the event for  upcoming Dates
            if (dayOfMonth.text.toString()
                    .toInt() == currentDay && mViewDataBinding?.monthYearTV?.text == currentMonth && mViewDataBinding?.yearTV?.text.toString()
                    .toInt() == date.year
            ) {
                dayOfMonth.setTextColor(Color.WHITE)
                dayOfMonth.setBackgroundResource(R.drawable.circle_red)
            }
        }

        // 2528 For Selecting Entire Week
        fun weekSelection(previousDates: ArrayList<Int>?, position: Int) {
            val date = days?.get(position)
            // if (date?.monthValue!! >= cmonth && date.year >= cyear)  {
            if (date != null) {
                if (date <= businessValidity) {
                    Log.d("TAG", "weekSelection: $absoluteAdapterPosition")
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
        private fun selDateHighlight(date: LocalDate, eventDate: LocalDate) {
            if (daytype == "Day" || daytype == "Week") {
                // 2528 Setting and highlighting the current Date
                if (date != CalendarUtils.selectedDate) {
                } else if (date.monthValue < cmonth + 1 && mViewDataBinding?.yearTV?.text.toString()
                        .toInt() == cyear
                ) {
                } else if (mViewDataBinding?.monthYearTV?.text == thisMonth && date.dayOfMonth < cDay && mViewDataBinding?.yearTV?.text.toString()
                        .toInt() == cyear
                ) {
                } else {
                    //                else  if(eventDate.year <= date.year) {
//                    if (eventDate.monthValue <= date.monthValue) {
//                        if (eventDate.dayOfMonth < date.dayOfMonth) {
//                            dayOfMonth.setTextColor(Color.GRAY)
//                        }
//                    }
//                }
                    if (date <= businessValidity) {
                        dayOfMonth.setTextColor(Color.WHITE)
                        dayOfMonth.setBackgroundResource(R.drawable.circle_fade_35)
                    }
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
            // 2692 hiding the previous date and month
            if (mViewDataBinding?.monthYearTV?.text == thisMonth && date?.dayOfMonth!! < cDay && mViewDataBinding?.yearTV?.text.toString()
                    .toInt() == cyear
            ) {
            } else if (date?.monthValue!! < cmonth + 1 && mViewDataBinding?.yearTV?.text.toString()
                    .toInt() == cyear
            ) {
            } else if (mViewDataBinding?.yearTV?.text.toString()
                    .toInt() < cyear
            ) {
            } else {
                onItemListener.onItemClick(
                    absoluteAdapterPosition,
                    days?.get(absoluteAdapterPosition),
                    dayinWeek, daysPositon, selectedWeekDates
                )
            }
        }
    }
}