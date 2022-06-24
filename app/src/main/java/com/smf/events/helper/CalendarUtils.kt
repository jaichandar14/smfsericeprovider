package com.smf.events.helper

import android.util.Log
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

// 2528
@Singleton
class CalendarUtils @Inject constructor() {
    companion object {
        var selectedDate: LocalDate? = null
        var dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        var updatedTabPosition = 0
    }

    fun monthYearFromDate(date: LocalDate): String? {
        val formatter = DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT_MONTH)
        return date.format(formatter)
    }

    fun monthYearFromDateFull(date: LocalDate): String? {
        val formatter = DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT_MONTH_YEAR)
        return date.format(formatter)
    }

    fun yearAndMonthFromDate(date: LocalDate): String? {
        val formatter = DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT_YEAR)
        return date.format(formatter)
    }


    // 2686 WeekDate
    data class WeekDates(var fromDate: String, var toDate: String, val weekList: ArrayList<String>)

    // 2686 Method for Getting Week Start and End Date
    fun fromAndToDate(): WeekDates {
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT)
        val firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        val startOfCurrentWeek: LocalDate =
            CalendarUtils.selectedDate!!.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
        val weekList = ArrayList<String>()
        for (i in 0 until 7) {
            weekList.add(startOfCurrentWeek.plusDays(i.toLong()).format(dateFormatter))
        }
        Log.d("TAG", "fromAndToDate: $weekList")
        val lastDayOfWeek: DayOfWeek = firstDayOfWeek.plus(6)
        val endOfWeek: LocalDate =
            CalendarUtils.selectedDate!!.with(TemporalAdjusters.nextOrSame(lastDayOfWeek))
        Log.d("TAG", "nextMonthAction: ${startOfCurrentWeek}")
        var fromDate = startOfCurrentWeek.format(dateFormatter)
        var toDate = endOfWeek.format(dateFormatter)
        return WeekDates(fromDate, toDate, weekList)
    }

    // 2686 MonthDates
    data class MonthDates(var fromDate: String, var toDate: String)

    //2686 Method fro getting From and to todate Fro a month
    fun monthFromAndToDate(): MonthDates {
        val fromDateMonth: LocalDate = CalendarUtils.selectedDate!!.withDayOfMonth(1)
        val toDateMonth: LocalDate =
            CalendarUtils.selectedDate!!.plusMonths(1).withDayOfMonth(1).minusDays(1)
        return MonthDates(
            fromDateMonth.format(CalendarUtils.dateFormatter),
            toDateMonth.format(CalendarUtils.dateFormatter)
        )
    }

    // 2528 daysInMonthArray Method
    fun daysInMonthArray(): ArrayList<LocalDate>? {
        val daysInMonthArray = ArrayList<LocalDate>()
        val yearMonth = YearMonth.from(selectedDate)
        val daysInMonth = yearMonth.lengthOfMonth()
        val prevMonth = selectedDate!!.minusMonths(1)
        val nextMonth = selectedDate!!.plusMonths(1)
        val prevYearMonth = YearMonth.from(prevMonth)
        val prevDaysInMonth = prevYearMonth.lengthOfMonth()
        val firstOfMonth = selectedDate!!.withDayOfMonth(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value
        for (i in 1..42) {
            if (i <= dayOfWeek) daysInMonthArray.add(
                LocalDate.of(
                    prevMonth.year,
                    prevMonth.month,
                    prevDaysInMonth + i - dayOfWeek
                )
            ) else if (i > daysInMonth + dayOfWeek) daysInMonthArray.add(
                LocalDate.of(
                    nextMonth.year,
                    nextMonth.month,
                    i - dayOfWeek - daysInMonth
                )
            ) else daysInMonthArray.add(
                LocalDate.of(
                    selectedDate!!.year, selectedDate!!.month, i - dayOfWeek
                )
            )
        }
        return daysInMonthArray
    }

    // 2743 Fetcching the entire week of a month
    fun fetchWeekOfMonth(): HashMap<Int, WeekDatesOfMonth> {
        // getting sundays  date of the month
        var j = 2
        var weeksList: HashMap<Int, WeekDatesOfMonth> = HashMap()
        val fromDateMonth: LocalDate = CalendarUtils.selectedDate!!.withDayOfMonth(1)
        val toDateMonth: LocalDate =
            CalendarUtils.selectedDate!!.plusMonths(1).withDayOfMonth(1).minusDays(1)
        val firstDayOfWeekSunday: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        val startOfFirstWeekSunday: LocalDate =
            fromDateMonth.with(TemporalAdjusters.previousOrSame(firstDayOfWeekSunday))
        val lastDayOfWeekSunday: DayOfWeek = firstDayOfWeekSunday.plus(6)
        val endLastDayOfWeekSunday: LocalDate =
            fromDateMonth.with(TemporalAdjusters.nextOrSame(lastDayOfWeekSunday))
        Log.d("TAG", "fetchWeekOfMonth: $endLastDayOfWeekSunday $")
        var poslist: Int = 0
        var endOfTheWeekMonthValue = endLastDayOfWeekSunday.monthValue
        var currentmonthvalue = LocalDateTime.now().monthValue

        if (endLastDayOfWeekSunday.dayOfMonth <= LocalDateTime.now().dayOfMonth && endOfTheWeekMonthValue <= currentmonthvalue && endLastDayOfWeekSunday.dayOfMonth <= LocalDateTime.now().dayOfMonth && endLastDayOfWeekSunday.year <= LocalDateTime.now().year) {
            //  weeksList.put(1, WeekDatesOfMonth(startOfFirstWeekSunday, endLastDayOfWeekSunday, poslist))
//j=1
        } else if (endLastDayOfWeekSunday.dayOfMonth <= LocalDateTime.now().dayOfMonth && endOfTheWeekMonthValue >= currentmonthvalue && endLastDayOfWeekSunday.year <= LocalDateTime.now().year) {
            weeksList.put(1,
                WeekDatesOfMonth(startOfFirstWeekSunday, endLastDayOfWeekSunday, poslist))
        } else {
            weeksList.put(1,
                WeekDatesOfMonth(startOfFirstWeekSunday, endLastDayOfWeekSunday, poslist))
        }

        // getting Saturdays  date of the month
        val endOfWeek: LocalDate =
            toDateMonth.with(TemporalAdjusters.previousOrSame(firstDayOfWeekSunday))
        val endOfMonthWeek: LocalDate =
            toDateMonth.with(TemporalAdjusters.nextOrSame(lastDayOfWeekSunday))
        var startweeklistSaturday = endLastDayOfWeekSunday
        var startweeklist = startOfFirstWeekSunday
        val currentDateSunday: LocalDate =
            LocalDate.now().with(TemporalAdjusters.previousOrSame(firstDayOfWeekSunday))

        for (i in 0 until 7) {
            if (startweeklist != endOfWeek && startweeklistSaturday != endOfMonthWeek) {
                startweeklist = startweeklist.plusDays(7)
                startweeklistSaturday = startweeklistSaturday.plusDays(7)
                weeksList.put(j,
                    WeekDatesOfMonth(startweeklist, startweeklistSaturday, poslist))
                j++
            }
        }
        var weeksListMap: ArrayList<Int> = ArrayList()
        weeksList.forEach {
            if (currentDateSunday > it.value.fromDate) {
                weeksListMap.add(it.key)
            }
        }
        weeksListMap.forEach {
            weeksList.remove(it)
        }
        return weeksList
    }
}

data class WeekDatesOfMonth(var fromDate: LocalDate, var toDate: LocalDate, var pos: Int?)
data class WeekDetails(var date: LocalDate?, var position: Int?)
data class WeekArrayDetails(var date: ArrayList<LocalDate>, var position: ArrayList<Int>)

object CalendarFormat {
    const val DAY = "Day"
    const val WEEK = "Week"
    const val MONTH = "Month"

    const val JAN = 1
    const val FEB = 2
    const val MAR = 3
    const val APR = 4
    const val MAY = 5
    const val JUN = 6


}

