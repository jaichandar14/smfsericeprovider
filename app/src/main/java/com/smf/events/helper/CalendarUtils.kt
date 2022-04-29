package com.smf.events.helper

import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

// 2528
@Singleton
class CalendarUtils @Inject constructor() {
    companion object {
        var selectedDate: LocalDate? = null
    }

    fun formattedDate(date: LocalDate): String? {
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
        return date.format(formatter)
    }

    fun formattedTime(time: LocalTime): String? {
        val formatter = DateTimeFormatter.ofPattern("hh:mm:ss a")
        return time.format(formatter)
    }

    fun formattedShortTime(time: LocalTime): String? {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return time.format(formatter)
    }

    fun monthYearFromDate(date: LocalDate): String? {
        val formatter = DateTimeFormatter.ofPattern("MMM")
        return date.format(formatter)
    }

    fun monthDayFromDate(date: LocalDate): String? {
        val formatter = DateTimeFormatter.ofPattern("MMMM d")
        return date.format(formatter)
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
            if (i <= dayOfWeek) daysInMonthArray.add(LocalDate.of(prevMonth.year,
                prevMonth.month,
                prevDaysInMonth + i - dayOfWeek)) else if (i > daysInMonth + dayOfWeek) daysInMonthArray.add(
                LocalDate.of(nextMonth.year,
                    nextMonth.month,
                    i - dayOfWeek - daysInMonth)) else daysInMonthArray.add(
                LocalDate.of(
                    selectedDate!!.year, selectedDate!!.month, i - dayOfWeek))
        }
        return daysInMonthArray
    }
}

data class WeekDetails(var date: LocalDate?, var position: Int?)
data class WeekArrayDetails(var date: ArrayList<LocalDate>, var position: ArrayList<Int>)