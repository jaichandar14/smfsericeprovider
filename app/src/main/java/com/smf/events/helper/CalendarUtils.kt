package com.smf.events.helper

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

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

    fun daysInWeekArray(): ArrayList<LocalDate>? {
        val days = ArrayList<LocalDate>()
        var current = selectedDate?.let { sundayForDate(it) }
        val endDate = current!!.plusWeeks(1)
        while (current!!.isBefore(endDate)) {
            days.add(current)
            current = current.plusDays(1)
        }
        return days
    }

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
