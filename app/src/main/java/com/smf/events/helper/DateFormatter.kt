package com.smf.events.helper

import java.time.Month

//2402 View Order details  Date Formatter
object DateFormatter {
    //2402- - Date Formatted for setting details
    fun getDateFormat(input: String): String {
        var monthCount = input.substring(0, 2)
        val date = input.substring(3, 5)
        val year = input.substring(6, 10)
        if (monthCount[0].digitToInt() == 0) {
            monthCount = monthCount[1].toString()
        }
        val month = Month.of(monthCount.toInt()).toString().substring(0, 3)
        return "$date $month $year"
    }
}